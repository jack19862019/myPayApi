package com.pay.rmi.paythird.yilufa;

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
import com.pay.rmi.common.utils.HttpsParams;
import com.pay.rmi.common.utils.LogByMDC;
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

@Service(YiLuFa.channelNo)
public class YiLuFa extends AbstractPay {

    static final String channelNo = "yilufa";

    private static final String payUrl = "https://ceob2b.net:8443/api/shopApi/order/createorder2";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public YiLuFa() {
        payTypeMap.put(OutChannel.alipay.name(), "alipay");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "易路发支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "易路发支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String code = resultMap.get("code");
        Assert.isTrue("0".equals(code), "易路发支付状态响应异常:" + resultMap.get("message"));

        String qrcode = resultMap.get("page_url");


        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrcode);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "易路发支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("shopAccountId", upMerchantNo);
        params.put("shopUserId", reqParams.getUserId());
        params.put("amountInString", amount);
        params.put("payChannel", payType);
        params.put("shopNo", orderNo);
        params.put("target", "3");
        params.put("apiVersion", "2");
        params.put("returnUrl", reqParams.getReturnUrl());
        params.put("shopCallbackUrl", getCallbackUrl(channelNo, merchNo, orderNo));

        LogByMDC.info(channelNo, "易路发支付参与加签内容：{}", params);
        String sign = Md5Utils.MD5(getSignStr(params, upPublicKey));
        assert sign != null;
        params.put("sign", sign);
        return params;
    }

    public static void main(String[] args) {

    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = params.get("shopAccountId") + "&" +
                params.get("shopUserId") + "&" +
                params.get("amountInString") + "&" +
                params.get("shopNo") + "&" +
                params.get("payChannel") + "&" + upPublicKey;
        LogByMDC.info(channelNo, "易路发支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "易路发支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "易路发支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "易路发支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String trade_no = params.get("shop_no");
        String status = params.get("status");
        String amount = params.get("money");

        if (!"0".equals(status)) {
            LogByMDC.error(channelNo, "易路发支付回调订单：{}，支付未成功，不再向下通知", trade_no);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "易路发支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "易路发支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
        }
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String signParams = params.get("shopAccountId") + "&" +
                params.get("status") + "&" +
                params.get("trade_no") + "&" +
                params.get("shop_no") + "&" +
                params.get("money") + "&" +
                params.get("type") + "&" + upMerchantKey;

        LogByMDC.info(channelNo, "易路发支付回调订单:{}，参与验签参数:{}", params.get("shop_no"), signParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(signParams));
        return newSign.equals(params.get("sign"));
    }

}
