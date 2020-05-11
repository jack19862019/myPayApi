package com.pay.rmi.paythird.tongfu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.DateUtil;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.HttpsParams;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 通付支付
 */
@Service(TongFu.channelNo)
public class TongFu extends AbstractPay {

    static final String channelNo = "tongfu";

    private static final String payUrl = "https://mmqp168tidan.699dz.com/api/order-add";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public TongFu() {
        payTypeMap.put(OutChannel.aliyssm.name(), "ALIPAYQR");//支付宝扫码
        payTypeMap.put(OutChannel.wechatyssm.name(), "WECHAT");//微信扫码

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) throws UnsupportedEncodingException, JsonProcessingException {
        LogByMDC.info(channelNo, "通付支付请求：{}", JSON.toJSONString(reqParams));
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
        Assert.notNull(payType, "通付支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        String stamp = System.currentTimeMillis() + "";
        stamp = stamp.substring(0, 10);

        Map<String, String> params = new TreeMap<>();
        params.put("merchant_id", upMerchantNo);//商户号
        params.put("order_no", orderNo);//商户订单号
        params.put("type", payType);//支付方式
        params.put("amount", String.valueOf(new BigDecimal(amount)));//交易金额(单位：元)
        params.put("user_identifier", reqParams.getUserId());//用户id
        params.put("callback_url", getCallbackUrl(channelNo, merchNo, orderNo));

        LogByMDC.info(channelNo, "通付支付参与加签内容：{}", params);
        String sign = getSignStr(params, upPublicKey).toUpperCase();
        params.put("sign", sign);
        params.put("return_url", reqParams.getReturnUrl());
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) throws UnsupportedEncodingException {
        LogByMDC.info(channelNo, "通付支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "通付支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "通付支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String tradeNo = params.get("order_no");
        String tradeStatus = params.get("status");
        String amount = params.get("amount");

        if (!"1".equals(tradeStatus)) {
            LogByMDC.error(channelNo, "通付支付回调订单：{}，支付未成功，不再向下通知", tradeNo);
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(tradeNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "通付支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "通付支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        System.out.println("通付支付下发通知成.........................");
        return "success";
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "通付支付参与MD5加签内容：{}", signParams);
        return Objects.requireNonNull(Md5Utils.MD5(signParams));
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "通付支付回调订单:{}，参与验签参数:{}", params.get("orderId"), signParams);
        String newSign = Md5Utils.MD5(signParams).toUpperCase();
        return newSign.equals(sign);
    }

}
