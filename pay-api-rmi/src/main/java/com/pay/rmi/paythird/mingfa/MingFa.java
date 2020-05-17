package com.pay.rmi.paythird.mingfa;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.HttpsParams;
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
 * 快银
 */
@Service(MingFa.channelNo)
public class MingFa extends AbstractPay {

    static final String channelNo = "mingfa";

    private static final String payUrl = "http://post.xyg01.com/pay/index";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public MingFa() {
        payTypeMap.put(OutChannel.wechatpay.name(), "1");
        payTypeMap.put(OutChannel.alipay.name(), "2");
        payTypeMap.put(OutChannel.unionsm.name(), "3");
        payTypeMap.put(OutChannel.unionh5.name(), "4");
        payTypeMap.put("wg", "5");
        payTypeMap.put(OutChannel.onlinepay.name(), "6");
        payTypeMap.put(OutChannel.wechath5.name(), "11");
        payTypeMap.put(OutChannel.alih5.name(), "21");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "明发支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        //发送支付请求
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "明发支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("code");
        Assert.isTrue("1".equals(code), "明发支付状态响应:" + resultMap.get("msg"));
        String data = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        String payUrl = resultData.get("pay_url");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(payUrl);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "明发支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("uid", upMerchantNo);
        params.put("istype", payType);
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("orderid", orderNo);
        params.put("orderuid", reqParams.getUserId());
        params.put("goodsname", new BigDecimal(amount).intValue() + "");//金额

        LogByMDC.info(channelNo, "明发支付参与加签内容：{}", params);
        String sign = Md5Utils.MD5(getSignStr(params, upPublicKey));
        params.put("key", sign);
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        params.put("token", upPublicKey);
        String signParams = SignUtils.buildParams2(params, true);
        LogByMDC.info(channelNo, "明发支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "明发支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "明发支付回调订单：{}，重复回调", order.getOrderNo());
            return "OK";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "明发支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String trade_no = params.get("orderid");
        //String trade_status = params.get("pay_status");
        String amount = params.get("price");

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "明发支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "明发支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
        }
        return "OK";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("key");
        params.put("token", upMerchantKey);
        String signParams = SignUtils.buildParams2(params, true);
        LogByMDC.info(channelNo, "明发支付回调订单:{}，参与验签参数:{}", params.get("orderid"), signParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(signParams));
        return newSign.equals(sign);
    }

}
