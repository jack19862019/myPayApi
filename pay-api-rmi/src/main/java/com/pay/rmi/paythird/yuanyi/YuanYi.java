package com.pay.rmi.paythird.yuanyi;

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
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.DateUtil;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
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
 * 缘一
 */
@Service(YuanYi.channelNo)
public class YuanYi extends AbstractPay {

    static final String channelNo = "yuanyi";

    private static final String payUrl = "https://uu.1357910.com/pay_Index_index.html";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public YuanYi() {
        payTypeMap.put(OutChannel.wechatpay.name(), "902");//微信扫码
        payTypeMap.put(OutChannel.alipay.name(), "903");//支付宝扫码
        payTypeMap.put(OutChannel.aliwap.name(), "904");//支付宝wap
        payTypeMap.put(OutChannel.wechath5.name(), "914");//微信h5
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "缘一支付请求：{}", JSON.toJSONString(reqParams));
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
        Assert.notNull(payType, "缘一不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("pay_memberid", upMerchantNo);
        params.put("pay_applydate", DateUtil.getCurrentStr());
        params.put("pay_orderid", orderNo);
        params.put("pay_amount", amount);
        params.put("pay_bankcode", payType);
        params.put("pay_callbackurl", reqParams.getReturnUrl());
        params.put("pay_notifyurl", getCallbackUrl(channelNo, merchNo, orderNo));
        String sign = Md5Utils.MD5(getSignStr(params, upPublicKey));
        assert sign != null;
        params.put("pay_md5sign", sign.toUpperCase());
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signatureContent = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "缘一支付支付加签参数：{}", signatureContent);
        return signatureContent;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "缘一支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "缘一支付回调订单：{}，重复回调", order.getOrderNo());
            return "ok";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "缘一支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }
        String trade_no = params.get("orderid");
        String returnCode = params.get("returncode");
        String amount = params.get("amount");

        if (!"00".equals(returnCode)) {
            LogByMDC.error(channelNo, "缘一支付回调订单：{}，支付未成功{}，不再向下通知", trade_no);
            return "ok";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "缘一支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "缘一支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("缘一支付下发通知报错:" + e.getMessage());
        }
        System.out.println("缘一支付下发通知成功..................................");
        return "ok";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String signParams = "amount=" + params.get("amount") + "&datetime=" + params.get("datetime") +
                "&memberid=" + params.get("memberid") + "&orderid=" + params.get("orderid") +
                "&returncode=" + params.get("returncode") + "&transaction_id=" + params.get("transaction_id");
        LogByMDC.info(channelNo, "缘一支付回调订单:{}，回调参与验签参数:{}", params.get("orderid"), signParams);
        String buildParams = signParams + "&key=" + upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        return newSign.equals(params.get("sign"));
    }

}
