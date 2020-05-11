package com.pay.rmi.paythird.rzhifu;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
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
import java.text.SimpleDateFormat;
import java.util.*;

@Service(RZhiFu.channelNo)
public class RZhiFu extends AbstractPay {
    public static final String channelNo = "rzhifu";

    static final String payUrl = "http://j1.rxpay.me/channel/common/mail_interface";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public RZhiFu(){
        payTypeMap.put(OutChannel.wechatHF.name(), "wechatHF");
        payTypeMap.put(OutChannel.alih5.name(), "alipaydj");
        payTypeMap.put(OutChannel.wechath5.name(), "wechatdj");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "利享支付，请求：{}", JSON.toJSONString(reqParams));

        //根据入参加工参数成利享支付需要的参数与签名
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
        Assert.notNull(payType, "R支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Map<String, String> params = new TreeMap<>();
        params.put("api_code", upMerchantNo);
        params.put("is_type", payType);
        params.put("order_id", orderNo);
        params.put("return_url", reqParams.getReturnUrl());
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("return_type", "json");
        DecimalFormat df = new DecimalFormat("#.00");
        params.put("price", df.format(new BigDecimal(amount)));
        params.put("time", sdf.format(new Date()));
        params.put("mark", reqParams.getMemo());
        String buildParams = SignUtils.buildParams(params) + "&key=" + upPublicKey;
        String sign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "R支付异步回调内容：{}", JSON.toJSONString(params));
        String amount = params.get("real_price");
        String systemNo = params.get("order_id");
        String paystatus = params.get("code");
        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "R支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "SUCCESS";
        }
        String upMerchantKey = mcpConfig.getUpKey();
        if (!"1".equals(paystatus)) {
            LogByMDC.error(channelNo, "R支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "SUCCESS";
        }
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "R支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "R支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "R支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "SUCCESS";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params);
        LogByMDC.info(channelNo, "R支付回调订单:{}，参与验签参数:{}", params.get("order_id"), signParams);
        String buildParams = signParams + "&key=" + upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        return newSign.equals(sign);
    }
}
