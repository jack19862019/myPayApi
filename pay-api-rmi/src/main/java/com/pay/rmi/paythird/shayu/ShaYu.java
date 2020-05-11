package com.pay.rmi.paythird.shayu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.HttpsParams;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 鲨鱼
 */
@Service(ShaYu.channelNo)
public class ShaYu extends AbstractPay {

    static final String channelNo = "shayu";

    private static final String payUrl = "http://api.icpayo.com/index/unifiedorder?format=json";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public ShaYu() {
        payTypeMap.put(OutChannel.alipay.name(), "alipay");//支付宝
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "鲨鱼支付请求：{}", JSON.toJSONString(reqParams));
        //根据入参加工参数成通汇宝支付需要的参数与签名
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());

        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);

        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "鲨鱼支付请求：{}",result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String payurl =resultMap.get("url");
        orderApiRespParams.setCode_url(payurl);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "鲨鱼支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();

        params.put("appid", upMerchantNo);
        params.put("version", "v1.1");
        params.put("pay_type", payType);
        BigDecimal money = new BigDecimal(amount);
        DecimalFormat df = new DecimalFormat("#.00");
        params.put("amount",df.format(money));
        params.put("success_url", reqParams.getReturnUrl());
        params.put("callback_url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("error_url", reqParams.getReturnUrl());
        params.put("out_uid", upMerchantNo);
        params.put("out_trade_no", orderNo);

        LogByMDC.info(channelNo, "鲨鱼支付参与加签内容：{}", params);
        String sign = Md5Utils.MD5(getSignStr(params, upPublicKey)).toUpperCase();
        params.put("sign", sign);
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParamsIgnoreNull(params) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "鲨鱼支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "鲨鱼支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "鲨鱼支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }
        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "鲨鱼支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }
        String trade_no = params.get("out_trade_no");
        String returnCode = params.get("callbacks");
        String amount = params.get("amount_true");

        if (!"CODE_SUCCESS".equals(returnCode)) {
            LogByMDC.error(channelNo, "鲨鱼支付回调订单：{}，支付未成功{}，不再向下通知", trade_no);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "鲨鱼支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "鲨鱼支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下鲨鱼支付发通知报错:" + e.getMessage());
        }
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        Map<String, String> paramTree = new TreeMap<>();
        paramTree.put("callbacks", params.get("callbacks"));
        paramTree.put("appid", params.get("appid"));
        paramTree.put("pay_type", params.get("pay_type"));
        paramTree.put("success_url", params.get("success_url"));
        paramTree.put("error_url", params.get("error_url"));
        paramTree.put("out_trade_no", params.get("out_trade_no"));
        paramTree.put("amount", params.get("amount"));
        paramTree.put("amount_true", params.get("amount_true"));
        paramTree.put("out_uid", params.get("out_uid"));
        String signParams = SignUtils.buildParamsIgnoreNull(paramTree) + "&key=" + upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(signParams)).toUpperCase();

        return newSign.equals(params.get("sign"));
    }

}
