package com.pay.rmi.paythird.angelpay;

import com.alibaba.fastjson.JSON;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 豆豆
 */
@Service(AngelPay.channelNo)
public class AngelPay extends AbstractPay {

    public static final String channelNo = "angelpay";

    static final String payUrl = "http://angelpay6.com/api/pay";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public AngelPay() {
        payTypeMap.put(OutChannel.alipay.name(), "alipay_qrcode");
        payTypeMap.put(OutChannel.alih5.name(), "alipay_app");

        payTypeMap.put(OutChannel.wechatpay.name(), "wechat_qrcode");
        payTypeMap.put(OutChannel.wechath5.name(), "wechat_app");

        payTypeMap.put(OutChannel.qqh5.name(), "qq_app");
        payTypeMap.put(OutChannel.qqpay.name(), "qq_qrcode");

        payTypeMap.put(OutChannel.jdpay.name(), "jd_qrcode");
        payTypeMap.put(OutChannel.jdh5.name(), "jd_app");

        payTypeMap.put(OutChannel.unionpay.name(), "onlinebank");
        payTypeMap.put(OutChannel.unionquickpay.name(), "yl_nocard");
        payTypeMap.put(OutChannel.unionsm.name(), "yl_qrcode");
        payTypeMap.put(OutChannel.unionh5.name(), "yl_app");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "AngelPay支付，请求：{}",  JSON.toJSONString(reqParams));
        //根据入参加工参数成百汇支付需要的参数与签名
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
        Assert.notNull(payType, "AngelPay支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("customerno", upMerchantNo);
        params.put("channeltype", payType);
        params.put("customerbillno", orderNo);
        params.put("orderamount", amount);
        params.put("customerbilltime", DateUtil.getCurrentStr());
        params.put("returnurl", reqParams.getReturnUrl());
        params.put("ip", reqParams.getReqIp());
        params.put("devicetype", "wap");
        params.put("customeruser", "yayaya2233@163.com");
        params.put("notifyurl", getCallbackUrl(channelNo, merchNo, orderNo));

        String buildParams = SignUtils.buildParams(params) + "&key=" + upPublicKey;
        String sign = Md5Utils.MD5(buildParams);
        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "AngelPay支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "AngelPay支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "OK";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "angelPay支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String amount = params.get("preorderamount");
        String systemNo = params.get("customerbillno");
        String paystatus = params.get("paystatus");
        if (!"SUCCESS".equals(paystatus)) {
            LogByMDC.error(channelNo, "angelPay支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "OK";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "AngelPay支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "AngelPay支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "OK";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        Map<String, String> treeMap = new TreeMap<>(params);
        String sign = treeMap.remove("sign");
        String signParams = SignUtils.buildParams(treeMap);
        LogByMDC.info(channelNo, "angelPay支付回调订单:{}，参与验签参数:{}", treeMap.get("customerbillno"), signParams);
        String buildParams = signParams + "&key=" + upMerchantKey;
        String newSign = Md5Utils.MD5(buildParams);
        return newSign.equals(sign);
    }
}
