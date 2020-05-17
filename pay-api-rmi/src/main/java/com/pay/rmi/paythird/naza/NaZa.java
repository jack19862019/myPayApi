package com.pay.rmi.paythird.naza;

import com.alibaba.fastjson.JSON;

import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service(NaZa.channelNo)
public class NaZa extends AbstractPay {

    public static final String channelNo = "naza";

    static final String payUrl = "http://pay.gzasww.com/pay";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public NaZa() {
        payTypeMap.put(OutChannel.alipay.name(), "0");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "哪吒支付请求：{}", JSON.toJSONString(reqParams));
        //根据入参加工参数成哪吒支付需要的参数与签名
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());

        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(payUrl, params));
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "哪吒支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new HashMap<>();
        params.put("uid", upMerchantNo);
        params.put("price", amount);
        params.put("paytype", payType);
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("return_url", reqParams.getReturnUrl());
        params.put("user_order_no", orderNo);

        String sign = getSignStr(params, upPublicKey);
        params.put("sign", sign);
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = params.get("uid") +
                params.get("price") +
                params.get("paytype") +
                params.get("notify_url") +
                params.get("return_url") +
                params.get("user_order_no") + upPublicKey;
        return Md5Utils.MD5(signParams);
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "哪吒支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "哪吒支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "OK";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "哪吒支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String amount = params.get("realprice");
        String systemNo = params.get("user_order_no");
        String paystatus = params.get("status");
        if (!"3".equals(paystatus)) {
            LogByMDC.error(channelNo, "哪吒支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "OK";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "哪吒支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "哪吒支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        System.out.println("哪吒支付异步回调成功.........................");
        return "OK";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = params.get("user_order_no") +
                params.get("orderno") +
                params.get("tradeno") +
                params.get("price") +
                params.get("realprice") + upMerchantKey;
        LogByMDC.info(channelNo, "哪吒支付回调订单:{}，参与验签参数:{}", params.get("user_order_no"), signParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(signParams));
        return newSign.equals(sign);
    }
}
