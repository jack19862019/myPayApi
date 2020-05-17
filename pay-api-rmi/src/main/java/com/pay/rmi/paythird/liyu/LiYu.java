package com.pay.rmi.paythird.liyu;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 鲤鱼
 */
@Service(LiYu.channelNo)
public class LiYu extends AbstractPay {
    static final String channelNo = "liyu";

    private static final String payUrl = "http://bdgyc.com/submit.php";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public LiYu() {
        payTypeMap.put(OutChannel.alipay.name(), "alipay");//支付宝
        payTypeMap.put(OutChannel.cft.name(), "tenpay");//财付通
        payTypeMap.put(OutChannel.qqpay.name(), "qqpay");//qq支付
        payTypeMap.put(OutChannel.wechatpay.name(), "wxpay");//微信支付
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "鲤鱼支付请求：{}", JSON.toJSONString(reqParams));

        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(payUrl, params));
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "鲤鱼支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        String stamp = System.currentTimeMillis() + "";
        stamp = stamp.substring(0, 10);

        Map<String, String> params = new TreeMap<>();
        params.put("pid", upMerchantNo);
        params.put("type", payType);
        params.put("out_trade_no", orderNo);
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("return_url", reqParams.getReturnUrl());
        params.put("name", reqParams.getProduct());
        params.put("money", amount);
        params.put("sign_type", "MD5");
        LogByMDC.info(channelNo, "鲤鱼支付参与加签内容：{}", params);
        String sign = getSignStr(params, upPublicKey);
        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "鲤鱼支付回调内容：{}", params);
        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "鲤鱼支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "鲤鱼支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String tradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String amount = params.get("money");

        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            LogByMDC.error(channelNo, "鲤鱼支付回调订单：{}，支付未成功，不再向下通知", tradeNo);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(tradeNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "鲤鱼支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "鲤鱼支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        System.out.println("鲤鱼支付下发通知成.........................");
        return "success";
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        Map<String, String>  map = params;
        map.remove("sign_type");
        String signParams = SignUtils.buildParams(map, true) + upPublicKey;
        LogByMDC.info(channelNo, "鲤鱼支付参与MD5加签内容：{}", signParams);
        return Objects.requireNonNull(Md5Utils.MD5(signParams));
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        params.remove("sign_type");
        String signParams = SignUtils.buildParams(params)+ upMerchantKey;
        LogByMDC.info(channelNo, "鲤鱼支付回调订单:{}，参与验签参数:{}", params.get("out_trade_no"), signParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(signParams));
        return newSign.equals(sign);
    }
}
