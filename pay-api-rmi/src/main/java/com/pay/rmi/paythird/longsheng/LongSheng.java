package com.pay.rmi.paythird.longsheng;

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
 * 隆晟支付
 */
@Service(LongSheng.channelNo)
public class LongSheng extends AbstractPay {

    static final String channelNo = "longsheng";

    private static final String payUrl = "http://www.longsheng168.vip/Pay_index.html";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public LongSheng() {
        payTypeMap.put(OutChannel.aliyssm.name(), "901");//支付宝扫码
        payTypeMap.put(OutChannel.alih5.name(), "902");//支付宝原生H5
        payTypeMap.put(OutChannel.wechatyssm.name(), "903");//微信扫码
        payTypeMap.put(OutChannel.wechath5.name(), "904");//微信H5
        payTypeMap.put(OutChannel.alipaygemah5.name(), "905");//支付宝个码H5
        payTypeMap.put(OutChannel.alipayzkh5.name(), "906");//支付宝转卡H5
        payTypeMap.put(OutChannel.alipayhuafei.name(), "907");//支付宝话费
        payTypeMap.put(OutChannel.wechathuafei.name(), "908");//微信话费

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) throws UnsupportedEncodingException, JsonProcessingException {
        LogByMDC.info(channelNo, "隆晟支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "隆晟支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {});
        String status = resultMap.get("status");
        Assert.isTrue("success".equals(status), "隆晟上游支付状态响应:" + resultMap.get("msg"));
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
        Assert.notNull(payType, "隆晟支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        String stamp = System.currentTimeMillis() + "";
        stamp = stamp.substring(0, 10);

        Map<String, String> params = new TreeMap<>();
        params.put("pay_memberid", upMerchantNo);//商户编号
        params.put("pay_orderid", orderNo);//商户订单号
        params.put("pay_amount", String.valueOf(new BigDecimal(amount).intValue()));//交易金额
        params.put("pay_applydate", DateUtil.toStr02(null, new Date()));//订单日期，格式：2018-01-02 01:30:40
        params.put("pay_bankcode", payType);//支付代码（支付方式）
        params.put("pay_notifyurl", getCallbackUrl(channelNo, merchNo, orderNo));//异步通知地址，如果不填则不通知
        params.put("pay_callbackurl", reqParams.getReturnUrl());
        LogByMDC.info(channelNo, "隆晟支付参与加签内容：{}", params);
        String sign = getSignStr(params, upPublicKey).toUpperCase();
        params.put("pay_attach", "123");//备注
        params.put("pay_md5sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) throws UnsupportedEncodingException {
        LogByMDC.info(channelNo, "隆晟支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "隆晟支付回调订单：{}，重复回调", order.getOrderNo());
            return "OK";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "JYD支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String tradeNo = params.get("orderid");
        String tradeStatus = params.get("returncode");
        String amount = params.get("amount");

        if (!"00".equals(tradeStatus)) {
            LogByMDC.error(channelNo, "隆晟支付回调订单：{}，支付未成功，不再向下通知", tradeNo);
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(tradeNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "隆晟支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "隆晟支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        System.out.println("隆晟支付下发通知成.........................");
        return "OK";
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "隆晟支付参与MD5加签内容：{}", signParams);
        return Objects.requireNonNull(Md5Utils.MD5(signParams));
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        params.remove("attach");
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "JYD支付回调订单:{}，参与验签参数:{}", params.get("orderId"), signParams);
        String newSign = Md5Utils.MD5(signParams).toUpperCase();
        return newSign.equals(sign);
    }

}
