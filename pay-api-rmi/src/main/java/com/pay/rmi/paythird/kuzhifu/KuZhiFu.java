package com.pay.rmi.paythird.kuzhifu;

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
@Service(KuZhiFu.channelNo)
public class KuZhiFu extends AbstractPay {

    static final String channelNo = "kuzhifu";

    private static final String payUrl = "http://api.305389.com/app/create_bill";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public KuZhiFu() {
        payTypeMap.put(OutChannel.alipay.name(), "1");
        payTypeMap.put(OutChannel.wechatpay.name(), "0");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "酷支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        //签名过期，等待加QQ群调试
        String result = restTemplate.postForObject(payUrl,  HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "酷支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("code");
        Assert.isTrue("0".equals(code), "酷支付状态响应:" + resultMap.get("msg"));
        String data = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        String signOld = resultData.remove("sign");
        String upPublicKey =mcpConfig.getUpKey();
        String signStr = getSignStr(resultData, upPublicKey);
        LogByMDC.info(channelNo, "酷支付响应参数验签参数，订单：{}，response：{}", reqParams.getOrderNo(), signStr);
        String sign = Objects.requireNonNull(Md5Utils.MD5(signStr)).toUpperCase();
        boolean equalStatus = sign.equals(signOld);
        Assert.isTrue(equalStatus, "酷支付请求验签失败");
        String card_qrcode = resultData.get("card_qrcode");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(card_qrcode);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "酷支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("app_key", upMerchantNo);
        params.put("sign_type", "MD5");
        params.put("bill_amount", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));
        params.put("out_trade_no", orderNo);
        params.put("qrcode_type", payType);

        String timpstamp = System.currentTimeMillis() + "";
        timpstamp = timpstamp.substring(0, 10);
        params.put("timestamp", timpstamp);

        params.put("nonce_str", MatchUtils.generateShortUuid());
        params.put("notice_url", getCallbackUrl(channelNo, merchNo, orderNo));

        params.put("bill_note", "下单");
        params.put("user_code", "365棋牌");

        LogByMDC.info(channelNo, "酷支付参与加签内容：{}", params);
        String sign = Md5Utils.MD5(getSignStr(params, upPublicKey));
        params.put("sign", sign.toUpperCase());
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&app_secret=" + upPublicKey;
        LogByMDC.info(channelNo, "酷支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "酷支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "酷支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        Map<String, String> resultData = JSON.parseObject(params.get("data"), new TypeReference<Map<String, String>>() {
        });
        LogByMDC.info(channelNo, "酷支付回调DATA内容：{}", resultData);

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(resultData, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "酷支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String trade_no = resultData.get("out_trade_no");
        String trade_status = params.get("pay_status");
        String amount = resultData.get("bill_amount");

        if (!"success".equals(trade_status)) {
            LogByMDC.error(channelNo, "酷支付回调订单：{}，支付未成功，不再向下通知", trade_no);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "酷支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "酷支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
        }
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        Map<String, String> treeMap = new TreeMap<>(params);
        String sign = treeMap.remove("sign");
        String signParams = SignUtils.buildParams(treeMap);
        String buildParams = signParams + "&app_secret=" + upMerchantKey;
        LogByMDC.info(channelNo, "酷支付回调订单:{}，参与验签参数:{}", treeMap.get("out_trade_no"), buildParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        return newSign.equals(sign);
    }

}
