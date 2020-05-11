package com.pay.rmi.paythird.naisi;

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
import com.pay.rmi.common.utils.*;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.pay.rmi.paythird.feiligu.SHA256WithRSAUtils;
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
@Service(NaiSi.channelNo)
public class NaiSi extends AbstractPay {

    static final String channelNo = "naisi";

    private static final String payUrl = "http://api.nszf.net/pay";

    private static final String rsaPrivateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCS2P/zWFPSyQKBUMrt+EPx3A9uVHjWgscpJcj11hNSmFeDM2MjuRPZj3EQUxzGBVB7uXbBG6/BWGhcJJ/WiufnOzc+PSng2y8tLLaUQk/55ID8NTO9C6UGhoyDkRseQPFIHDkVHWRRAXzUx+QoQxQno4fLwCPCfa9rGUqTfxiNaif9N3kkGGkLkbpN5M+pxbRrK2yb91/DqAIIeE1BgY4IHxWe5IXq/0I3qxDohQYJDaF0RzELSL5aG4uCBw+Fl+2XIoJ84WMEFlTefOxftlHQpi2v4ZKQBWnOCLPii3mCz+LhqKZzHigQomYgydQCVaavwlp3p5yNw6nlWI2LoO97AgMBAAECggEBAItmTcquzZvqT2N807cFl1JQSaG9tJxFt1Q7V0LvvpYIogKdVH6D4fEEdxs9GrJsNxPMYow8gSJ0j1TxO36JjE14bB+8JjYbO/SRXpztOkiJf1xsELpkBHtmXJtEs06L7cA7coC1zxYK6HqBavhBfS+H0JA9zHSDpXNwpdfQrfkHHeL2SG6FSLmr51ANehEENpQqCl7U4UwtBYtAAiJ80HAv1MgIrfSrLdehjNR7HVwh5AM35Tzkbu1KfLD7qG2SyUNPwfnAVu8sDBUa6zKXhC7k+HJSYGehgQCMRqPSo0lhQjIGZoJap4CttqijgeNdwj7Tvj/vUifVkUManREW/0ECgYEAwm2SA1Kdp/V/84euc+RMpAUthKvF7+YivdTClP2Fia9+5hTonopzGxrKPU5kJyiNR/gctM3ftg+2bcsA4p4vx/zrqv5AVzacPro5yJR4uqb9ZeLFB9AafUZ0WTO1EDe11GNc1oyc2QLUxrx66QDzg8lWslQ9DbrG3VTvUqNOqZsCgYEAwVoNoI99dwbIvLSOYkM9SvGO7NGtD2ydYHINZwsw3FuN7qGVy/23IXnm7VQJFlDgzVbm3yA1TgXTp5MIp63a4hjd08GX9jKEq0CjefyM8WvYxWap5eP13fEswLL0lduMgHh93ynfnmI1AguFPqp2NM5CBqzfOifK4o9ATG0Dn6ECgYEAgRxB2fMCvoZIcW+oaU0xl7nYgEGRL72jVGv0yvuPsd7YZfF8h8topJcltZBY8af8Kc6ZQXd1F3FXs5EbBWIGWMebt/EeyMt69iy0Mn5lrY02uHHeZVHMRWSD3rh3hE16cY+KFu9JONKoeVCwTgpTqzAIYmOOJ4/gfxnn0vifRAECgYEAh10+kRwkIIKVYtedn1mnvaIkB9Q7rsLB41W14GBLUtqeIWTq9Kep4Dvx2lGtEvyIhfnDJI7uArO9nGIp+wqSZRw3oRq2At0XWLWmPa78FcEFQB5B7/qKlVlVlYs0BP5x/TdeGDC+p6nlporePCMnIo0MWJQKrDJTrg3QXDEuCkECgYBMKQC18ywtRfUbXjl2hZo28ju6sNrXNM4wcrw+HEjvWn0/TIPw+qGf3XgT8G2ox3DnZPGjLobTy8SEBURiBVpCYssIFgmUipY9ExhzwfVq5HepaqNVtzT7cMqrEK7ismV+rTHAG2KAXVJoPaHtANGA9XrGxpkjPUUGLDpwV4hzcg==";

    private static final String rsaPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAktj/81hT0skCgVDK7fhD8dwPblR41oLHKSXI9dYTUphXgzNjI7kT2Y9xEFMcxgVQe7l2wRuvwVhoXCSf1orn5zs3Pj0p4NsvLSy2lEJP+eSA/DUzvQulBoaMg5EbHkDxSBw5FR1kUQF81MfkKEMUJ6OHy8Ajwn2vaxlKk38YjWon/Td5JBhpC5G6TeTPqcW0aytsm/dfw6gCCHhNQYGOCB8VnuSF6v9CN6sQ6IUGCQ2hdEcxC0i+WhuLggcPhZftlyKCfOFjBBZU3nzsX7ZR0KYtr+GSkAVpzgiz4ot5gs/i4aimcx4oEKJmIMnUAlWmr8Jad6ecjcOp5ViNi6DvewIDAQAB";

    private static final String shPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkT7pvrJcBWmn2uvthMAc+rXl+hf643SKy0mOGVwZi1q3RgCt1htKku1hciK5KRa34epEgNLg8mlx7KVDEs8OfulsWcTHaxDxQAku+3dQYKAl3tGVJbfOMAOmk9HFQeGTzID5CdZ8qt+FcLSlStqFChb4/5G+7OWmzs52Y+wlnYT3odDdNDUPbu+n7vhuZfu4PevZQde05vLkXXmyu7ImgTtiWljs+DRcIKNJKfKewatj0qfUbxGBAUYGURu01G0bt+uvg85XCsU6sAZuXKMEQcYMTidV3KPmMS2SFXc/n0/Y6B1+YiEERl/8FJFPlkL8vBqEnro9nbrDY338ZdPj7wIDAQAB";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public NaiSi() {
        payTypeMap.put(OutChannel.aliwap.name(), "Alipay_wap");
        payTypeMap.put(OutChannel.alipay.name(), "Alipay_QRcode");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "耐思支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "耐思支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);

        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String code = resultMap.get("code");
        Assert.isTrue(!"0".equals(code), "耐思支付状态响应:" + resultMap.get("msg"));

        String data = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });

        String qrCode = resultData.get("payurl");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrCode);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "耐思支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("merId", upMerchantNo);
        params.put("orderAmt", amount);
        params.put("orderId", orderNo);
        params.put("desc", reqParams.getProduct());
        params.put("channel", payType);
        params.put("nonceStr", MatchUtils.generateShortUuid());
        params.put("ip", reqParams.getReqIp());
        params.put("returnUrl", reqParams.getReturnUrl());
        params.put("notifyUrl", getCallbackUrl(channelNo, merchNo, orderNo));

        LogByMDC.info(channelNo, "耐思支付参与加签内容：{}", params);
        String sign = getSignStr(params, upPublicKey);

        params.put("sign", sign);

        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "耐思支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "耐思支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "耐思支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String tradeNo = params.get("orderId");
        String tradeStatus = params.get("status");
        String amount = params.get("orderAmt");

        if (!"1".equals(tradeStatus)) {
            LogByMDC.error(channelNo, "耐思支付回调订单：{}，支付未成功，不再向下通知", tradeNo);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(tradeNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "耐思支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "耐思支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
        }
        System.out.println("耐思支付下发通知成.........................");
        return "success";
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "耐思支付参与MD5加签内容：{}", signParams);
        String md5Str = Objects.requireNonNull(Md5Utils.MD5(signParams)).toUpperCase();
        return SHA256WithRSAUtils.buildRSASignByPrivateKey(md5Str,rsaPrivateKey);
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params);
        String buildParams = signParams + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "耐思支付回调订单:{}，参与验签参数:{}", params.get("orderId"), buildParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        return SHA256WithRSAUtils.buildRSAverifyByPublicKey(newSign,shPublicKey,sign);
    }

}
