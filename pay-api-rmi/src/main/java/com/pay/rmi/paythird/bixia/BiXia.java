package com.pay.rmi.paythird.bixia;

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
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 快银
 */
@Service(BiXia.channelNo)
public class BiXia extends AbstractPay {

    static final String channelNo = "bixia";

    private static final String payUrl = "http://bx1010.wgquan.cn/sk-pay/public/createPayOrder";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public BiXia() {
        payTypeMap.put(OutChannel.alipay.name(), "3201");//MY支付宝扫码（企业码）
        payTypeMap.put(OutChannel.wechatpay.name(), "3105");//微信扫码
        payTypeMap.put(OutChannel.onlinepay.name(), "3303");//EMC云闪付
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig , OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "陛下支付请求：{}", JSON.toJSONString(reqParams));
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
        Assert.notNull(payType, "陛下不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("ORDER_ID", orderNo);
        params.put("ORDER_AMT",  new DecimalFormat("#.00").format(new BigDecimal(amount)));
        params.put("USER_ID", upMerchantNo);
        params.put("BUS_CODE", payType);
        params.put("PAGE_URL", reqParams.getReturnUrl());
        params.put("BG_URL", getCallbackUrl(channelNo, merchNo, orderNo));
        String sign = getSignStr(params, upPublicKey);
        params.put("SIGN", sign);
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signStr = params.get("ORDER_ID") + params.get("ORDER_AMT") + params.get("USER_ID") + params.get("BUS_CODE");
        String s1 = Md5Utils.MD5(signStr);
        String s2 = Md5Utils.MD5(s1 + upPublicKey);
        String s3 = s2.substring(8, 24);
        LogByMDC.info(channelNo, "陛下支付支付加签参数：{}", signStr);
        return s3;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "陛下支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "陛下支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }
        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "陛下支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }
        String trade_no = params.get("ORDER_ID");
        String returnCode = params.get("TRANS_STATUS");
        String amount = params.get("AMOUNT");

        if (!"success".equals(returnCode)) {
            LogByMDC.error(channelNo, "陛下支付回调订单：{}，支付未成功{}，不再向下通知", trade_no);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "陛下支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "陛下支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下陛下支付发通知报错:" + e.getMessage());
        }
        System.out.println("成功下发通知.............................................");
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String verifySign = params.remove("SIGN");
        String signStr = params.get("ORDER_ID") + params.get("ORDER_AMT") + params.get("BUS_CODE");
        LogByMDC.info(channelNo, "陛下支付回调订单:{}，回调参与验签参数:{}", params.get("ORDER_ID"), signStr);
        String s1 = Md5Utils.MD5(signStr);
        String s2 = Md5Utils.MD5(s1 + upMerchantKey);
        assert s2 != null;
        String sign = s2.substring(8, 24);
        return sign.equals(verifySign);
    }

}
