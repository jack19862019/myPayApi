package com.pay.rmi.paythird.jialian;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.DateUtil;
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
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 嘉联支付
 */
@Service(JiaLian.channelNo)
public class JiaLian extends AbstractPay {

    static final String channelNo = "jialian";

    private static final String payUrl = "http://www.tomatolike1997.top/Pay_index.html";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public JiaLian() {
        payTypeMap.put(OutChannel.unionpay.name(), "912");//云闪付扫码
        payTypeMap.put(OutChannel.alih5.name(), "904");//支付宝H5
        payTypeMap.put(OutChannel.aliyssm.name(), "903");//支付宝扫码支付
        payTypeMap.put(OutChannel.wechatyssm.name(), "902");//微信扫码支付
        payTypeMap.put(OutChannel.wechath5.name(), "901");//微信H5
    }


    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "嘉联支付请求：{}", JSON.toJSONString(reqParams));

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
        Assert.notNull(payType, "嘉联支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();
        Map<String, String> params = new TreeMap<>();

        params.put("pay_memberid", upMerchantNo);//商户ID
        params.put("pay_orderid", orderNo);//订单ID
        params.put("pay_amount",  new DecimalFormat("#.00").format(new BigDecimal(amount)));
        params.put("pay_applydate", DateUtil.toStr02(null, new Date()));//申请时间，格式：2018-01-02 01:30:40
        params.put("pay_bankcode", payType);//支付代码（支付方式）
        params.put("pay_callbackurl", reqParams.getReturnUrl());
        params.put("pay_notifyurl", getCallbackUrl(channelNo, merchNo, orderNo));//异步通知地址，如果不填则不通知

        LogByMDC.info(channelNo, "嘉联支付参与加签内容：{}", params);
        String sign = Md5Utils.MD5(getSignStr(params, upPublicKey)).toUpperCase();
        assert sign != null;
        params.put("pay_md5sign", sign);
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "嘉联支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "嘉联支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "嘉联支付回调订单：{}，重复回调", order.getOrderNo());
            return "OK";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "嘉联支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String trade_no = params.get("orderId");
        String trade_status = params.get("returncode");
        String amount = params.get("amount");

        if (!"00".equals(trade_status)) {
            LogByMDC.error(channelNo, "嘉联支付回调订单：{}，支付未成功，不再向下通知", trade_no);
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "嘉联支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "嘉联支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return "OK";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        params.remove("attach");
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "嘉联支付回调订单:{}，参与验签参数:{}", params.get("orderId"), signParams);
        String newSign = Md5Utils.MD5(signParams).toUpperCase();
        return newSign.equals(sign);
    }

}
