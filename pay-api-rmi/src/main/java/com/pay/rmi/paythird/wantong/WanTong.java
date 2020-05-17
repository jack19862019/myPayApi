package com.pay.rmi.paythird.wantong;

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
import com.pay.rmi.common.utils.*;
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

@Service(WanTong.channelNo)
public class WanTong extends AbstractPay {

    static final String channelNo = "wantong";

    private static final String payUrl = "http://pay.wtzf.xyz/v1/pay";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public WanTong() {
        payTypeMap.put(OutChannel.alipay.name(), "904");
        payTypeMap.put(OutChannel.alih5.name(), "903");
        payTypeMap.put(OutChannel.wechatpay.name(), "902");
        payTypeMap.put(OutChannel.wechath5.name(), "901");
        payTypeMap.put(OutChannel.qqpay.name(), "906");
        payTypeMap.put(OutChannel.qqh5.name(), "905");
        payTypeMap.put(OutChannel.unionsm.name(), "910");

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "万通支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);


        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "万通支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String code = resultMap.get("code");
        Assert.isTrue("0000".equals(code), "万通支付状态响应:" + resultMap.get("msg"));

        String qrcode = resultMap.get("qrcode");

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());

        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrcode);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "万通支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("pay_memberid", upMerchantNo);
        params.put("pay_orderid", orderNo);
        params.put("pay_amount", amount);
        params.put("pay_applydate", DateUtil.getCurrentStr());
        params.put("pay_bankcode", payType);
        params.put("pay_format", "pay_str");
        params.put("pay_clientip", reqParams.getReqIp());
        params.put("pay_callbackurl", reqParams.getReturnUrl());
        params.put("pay_notifyurl", getCallbackUrl(channelNo, merchNo, orderNo));

        LogByMDC.info(channelNo, "万通支付参与加签内容：{}", params);
        String sign = Md5Utils.MD5(getSignStr(params, upPublicKey));
        assert sign != null;
        params.put("pay_md5sign", sign.toUpperCase());
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "万通支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "万通支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "万通支付回调订单：{}，重复回调", order.getOrderNo());
            return "SUCCESS";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "万通支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "FAIL";
        }

        String trade_no = params.get("orderid");
        String trade_status = params.get("returncode");
        String amount = params.get("amount");

        if (!"0000".equals(trade_status)) {
            LogByMDC.error(channelNo, "万通支付回调订单：{}，支付未成功，不再向下通知", trade_no);
            return "SUCCESS";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "万通支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "万通支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
        }
        return "SUCCESS";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params, true);
        String buildParams = signParams + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "万通支付回调订单:{}，参与验签参数:{}", params.get("orderid"), buildParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        return newSign.equals(sign);
    }

}
