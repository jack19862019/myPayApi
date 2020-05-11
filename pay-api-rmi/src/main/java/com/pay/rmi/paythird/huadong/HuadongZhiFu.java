package com.pay.rmi.paythird.huadong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
import com.pay.rmi.common.utils.HttpsParams;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.MatchUtils;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.*;

/**
 * 华东支付
 */
@Service(HuadongZhiFu.channelNo)
public class HuadongZhiFu extends AbstractPay {

    static final String channelNo = "huadongzhifu";

    private static final String payUrl = "https://api.xpay007.com/gateway/request_payurl/";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public HuadongZhiFu() {
        payTypeMap.put(OutChannel.wechatyssm.name(), "pay.weixin.scan");//微信扫码支付
        payTypeMap.put(OutChannel.wechath5.name(), "pay.weixin.h5");//微信H5
        payTypeMap.put(OutChannel.aliyssm.name(), "pay.alipay.scan");//支付宝扫码支付
        payTypeMap.put(OutChannel.alih5.name(), "pay.alipay.h5");//支付宝H5
        payTypeMap.put(OutChannel.unionsm.name(), "pay.unionpay.scan");//银联扫码
        payTypeMap.put(OutChannel.quickpayh5.name(), "pay.unionpay.h5");//银联H5
        payTypeMap.put(OutChannel.unionquickpay.name(), "pay.express");//快捷支付

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "华东支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        //签名过期，等待加QQ群调试
        String result = restTemplate.postForObject(payUrl,  HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "华东支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("code");
        Assert.isTrue("00".equals(code), "华东支付状态响应:" + resultMap.get("msg"));
        String qrCode = resultMap.get("pay_url");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrCode);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "华东支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("merchant_code", upMerchantNo);
        params.put("service_type", payType);
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("return_url", reqParams.getReturnUrl());
        params.put("client_ip", reqParams.getReqIp());
        params.put("order_no", orderNo);
        params.put("order_time", DateUtil.toStr03(null, new Date()));
        params.put("amount", String.valueOf(new BigDecimal(amount)));//金额，精确后小数点后两位
        params.put("coin_type", "CNY");
        params.put("product_name", "apple");

        LogByMDC.info(channelNo, "华东支付参与加签内容：{}", params);
        String sign = Md5Utils.MD5(getSignStr(params, upPublicKey));
        params.put("sign", sign.toUpperCase());
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "华东支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "华东支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "华东支付回调订单：{}，重复回调", order.getOrderNo());
            return "ok";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "华东支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String trade_no = params.get("order_no");
        String trade_status = params.get("status");
        String amount = params.get("amount");

        if (!"00".equals(trade_status)) {
            LogByMDC.error(channelNo, "华东支付回调订单：{}，支付未成功，不再向下通知", trade_no);
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "华东支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "华东支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return "ok";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "华东支付回调订单:{}，参与验签参数:{}", params.get("order_no"), signParams);
        String newSign = Md5Utils.MD5(signParams).toUpperCase();
        return newSign.equals(sign);
    }

}
