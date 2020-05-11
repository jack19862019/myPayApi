package com.pay.rmi.paythird.fangxinfu;

import com.alibaba.fastjson.JSON;

import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
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

/**
 * 快银
 */
@Service(FangXinFu.channelNo)
public class FangXinFu extends AbstractPay {

    static final String channelNo = "fangxinfu";

    private static final String payUrl = "https://www.91fxf.com/Pay_Index.html";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public FangXinFu() {
        payTypeMap.put(OutChannel.unionwap.name(), "1");
        payTypeMap.put(OutChannel.wechatpay.name(), "2");
        payTypeMap.put(OutChannel.alipay.name(), "3");
        payTypeMap.put(OutChannel.qqpay.name(), "4");
        payTypeMap.put(OutChannel.onlinepay.name(), "5");
        payTypeMap.put(OutChannel.baidupay.name(), "6");
        payTypeMap.put(OutChannel.jdpay.name(), "7");
        payTypeMap.put(OutChannel.wechatwap.name(), "8");
        payTypeMap.put(OutChannel.aliwap.name(), "9");
        payTypeMap.put(OutChannel.qqwap.name(), "10");
        payTypeMap.put(OutChannel.unionsm.name(), "11");
        payTypeMap.put(OutChannel.jdwap.name(), "12");
        payTypeMap.put(OutChannel.baiduwap.name(), "13");
        payTypeMap.put(OutChannel.unionquickpay.name(), "14");
        payTypeMap.put(OutChannel.wechath5.name(), "15");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "放心付支付，请求：{}", JSON.toJSONString(reqParams));
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
        Assert.notNull(payType, "放心付支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("pay_memberid", upMerchantNo);
        params.put("pay_amount", amount);
        params.put("pay_orderid", orderNo);
        params.put("pay_applydate", DateUtil.getCurrentStr());
        params.put("pay_bankcode", "0");
        params.put("tongdao", "0");
        params.put("cashier", payType);
        params.put("pay_callbackurl", reqParams.getReturnUrl());
        params.put("pay_notifyurl", getCallbackUrl(channelNo, merchNo, orderNo));

        LogByMDC.info(channelNo, "放心付支付参与加签内容：{}", params);
        Map<String, String> signMap = new HashMap<>(params);
        String sign = Objects.requireNonNull(Md5Utils.MD5(getSignStr(signMap, upPublicKey))).toUpperCase();
        params.put("pay_md5sign", sign);
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        params.remove("tongdao");
        params.remove("cashier");
        String signParams = SignUtils.buildParams3(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "放心付支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "放心付支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "放心付支付回调订单：{}，重复回调", order.getOrderNo());
            return "OK";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "放心付支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "FAIL";
        }

        String trade_no = params.get("orderid");
        String trade_status = params.get("returncode");
        String amount = params.get("amount");

        if (!"00".equals(trade_status)) {
            LogByMDC.error(channelNo, "放心付支付回调订单：{}，支付未成功，不再向下通知", trade_no);
            return "OK";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "放心付支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "放心付支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        System.out.println("放心付支付下发通知成.........................");
        return "OK";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        params.remove("reserved1");
        params.remove("reserved2");
        params.remove("reserved3");
        String signParams = SignUtils.buildParams3(params, true);
        String buildParams = signParams + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "放心付支付回调订单:{}，参与验签参数:{}", params.get("orderid"), buildParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        return newSign.equals(sign);
    }

}
