package com.pay.rmi.paythird.tonghuibao;

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
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;


@Service(TongHuiBao.channelNo)
public class TongHuiBao extends AbstractPay {

    public static final String channelNo = "tonghuibao";

    static final String payUrl = "http://pay.tonghuibaoabc.com/index/pay/gateway";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public TongHuiBao() {
        payTypeMap.put(OutChannel.alipay.name(), "904");
        payTypeMap.put(OutChannel.wechatpay.name(), "902");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "通汇宝支付请求：{}", JSON.toJSONString(reqParams));
        //根据入参加工参数成通汇宝支付需要的参数与签名
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "通汇宝支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);

        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("code");
        Assert.isTrue("1".equals(code), "通汇宝支付状态响应:" + resultMap.get("msg"));

        String data = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        String pay_extends = resultData.get("pay_extends");
        Assert.isTrue(!StringUtils.isEmpty(pay_extends), "通汇宝支付响应未返回支付二维码");

        Map<String, String> resultPayData = JSON.parseObject(pay_extends, new TypeReference<Map<String, String>>() {
        });
        String payUrl = resultPayData.get("pay_url");
        Assert.isTrue(!StringUtils.isEmpty(payUrl), "通汇宝支付响应未返回支付二维码");

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(payUrl);
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "通汇宝支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("user_id", upMerchantNo);
        params.put("product_id", payType);
        params.put("out_trade_no", orderNo);
        params.put("return_url", reqParams.getReturnUrl());
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("subject", reqParams.getProduct());
        params.put("pay_amount", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));
        params.put("applydate", DateUtil.getCurrentStr());
        params.put("remark", reqParams.getMemo());
        String buildParams = SignUtils.buildParams(params) + "&apikey=" + upPublicKey;
        String sign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "通汇宝支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "通汇宝支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "OK";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "通汇宝支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String amount = params.get("transaction_money");
        String systemNo = params.get("out_trade_no");
        String paystatus = params.get("returncode");
        if (!"00".equals(paystatus)) {
            LogByMDC.error(channelNo, "通汇宝支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "OK";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "通汇宝支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "通汇宝支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "OK";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params);
        LogByMDC.info(channelNo, "通汇宝支付回调订单:{}，参与验签参数:{}", params.get("out_trade_no"), signParams);
        String buildParams = signParams + "&apikey=" + upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        return newSign.equals(sign);
    }
}
