package com.pay.rmi.paythird.baihuizhifu;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service(BaiHui.channelNo)
public class BaiHui extends AbstractPay {

    public static final String channelNo = "baihui";

    static final String payUrl = "http://api.baihuiapi.net/interface/chargebank.aspx";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public BaiHui() {
        payTypeMap.put(OutChannel.alipay.name(), "ALIPAY");
        payTypeMap.put(OutChannel.cft.name(), "TENPAY");
        payTypeMap.put(OutChannel.wechatpay.name(), "WEIXIN");
        payTypeMap.put(OutChannel.wechath5.name(), "WXWAP");
        payTypeMap.put(OutChannel.wechatgz.name(), "WXAPP");
        payTypeMap.put(OutChannel.alih5.name(), "ALIWAP");
        payTypeMap.put(OutChannel.qqpay.name(), "QQCODE");
        payTypeMap.put(OutChannel.qqh5.name(), "QQWAP");
        payTypeMap.put(OutChannel.qqwap.name(), "QQAPP");
        payTypeMap.put(OutChannel.jdpay.name(), "JINGDONG");
        payTypeMap.put(OutChannel.jdh5.name(), "JDWAP");
        payTypeMap.put(OutChannel.unionsm.name(), "VISA");
        payTypeMap.put(OutChannel.unionpay.name(), "KUAIJIE");

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "百汇支付，请求：{}", JSON.toJSONString(reqParams));
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
        Assert.notNull(payType, "百汇支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new HashMap<>();
        params.put("orderid", orderNo);
        params.put("value", amount);
        params.put("type", payType);
        params.put("parter", upMerchantNo);
        params.put("callbackurl", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("hrefbackurl", reqParams.getReturnUrl());
        params.put("user_order_no", orderNo);

        String sign = getSignStr(params, upPublicKey);
        params.put("sign", sign);
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = "parter=" + params.get("parter") +
                "&type=" + params.get("type") +
                "&orderid=" + params.get("orderid") +
                "&callbackurl=" + params.get("callbackurl") + upPublicKey;
        return Md5Utils.MD5(signParams);
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "百汇支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "百汇支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "ok";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "百汇支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String amount = params.get("ovalue");
        String systemNo = params.get("orderid");
        String paystatus = params.get("restate");
        if (!"0".equals(paystatus)) {
            LogByMDC.error(channelNo, "百汇支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "ok";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "百汇支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "百汇支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        LogByMDC.info(channelNo, "百汇异步回调成功.........................");
        return "ok";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = "orderid=" + params.get("orderid") +
                "&restate=" + params.get("restate") +
                "&ovalue=" + params.get("ovalue") + upMerchantKey;
        LogByMDC.info(channelNo, "百汇支付回调订单:{}，参与验签参数:{}", params.get("orderid"), signParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(signParams));
        return newSign.equals(sign);
    }
}
