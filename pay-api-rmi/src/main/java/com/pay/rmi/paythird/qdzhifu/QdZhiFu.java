package com.pay.rmi.paythird.qdzhifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.enums.OrderStatus;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * QD支付
 */
@Service(QdZhiFu.channelNo)
public class QdZhiFu extends AbstractPay {

    static final String channelNo = "qdzhifu";

    private static final String payUrl = "http://pay11.chongwu-tkd.cn/api/addOrder";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public QdZhiFu() {
        payTypeMap.put(OutChannel.alih5.name(), "zfbh5");//支付宝H5
        payTypeMap.put(OutChannel.wechatpay.name(), "wei1");//微信轮询
        payTypeMap.put(OutChannel.wechath5.name(), "Wei2");//微信固额
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "QD支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        //签名过期，等待加QQ群调试
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "QD支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String resultStr = resultMap.get("result");
        Map<String, String> resultData = JSON.parseObject(resultStr, new TypeReference<Map<String, String>>() {
        });

        String code = resultMap.get("code");
        Assert.isTrue("0000".equals(code), "QD支付状态响应:" + resultMap.get("msg"));
        String successStatus = resultMap.get("success");
        Assert.isTrue("true".equals(successStatus), "QD支付状态响应:" + resultMap.get("msg"));

        //响应验签
        String signOld = resultData.remove("sign");
        String upPublicKey = mcpConfig.getUpKey();
        String signStr = getSignStr(resultData, upPublicKey);
        LogByMDC.info(channelNo, "QD支付响应参数验签参数，订单：{}，response：{}", reqParams.getOrderNo(), signStr);
        String sign = Objects.requireNonNull(Md5Utils.SHA(signStr)).toUpperCase();
        boolean equalStatus = sign.equals(signOld);
        Assert.isTrue(equalStatus, "QD支付请求验签失败");

        String qrCode = resultData.get("qrUrl");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrCode);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "QD支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("merchant", upMerchantNo);
        params.put("amount", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));
        params.put("pay_type", payType);
        params.put("order_no", orderNo);
        String timpstamp = System.currentTimeMillis() + "";
        timpstamp = timpstamp.substring(0, 10);
        params.put("order_time", timpstamp);
        params.put("subject", reqParams.getProduct());
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("return_url", reqParams.getReturnUrl());

        LogByMDC.info(channelNo, "QD支付参与加签内容：{}", params);
        String sign = Md5Utils.SHA(getSignStr(params, upPublicKey)).toUpperCase();
        assert sign != null;
        params.put("sign", sign);
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "QD支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "QD支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "QD支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "QD支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String trade_no = params.get("orderId");
        //String trade_status = params.get("pay_status");
        String amount = params.get("amount");

        /*if (!"success".equals(trade_status)) {
            LogByMDC.error(channelNo, "AA支付回调订单：{}，支付未成功，不再向下通知", trade_no);
            return "success";
        }*/

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "QD支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "QD支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params);
        String buildParams = signParams + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "QD支付回调订单:{}，参与验签参数:{}", params.get("orderId"), buildParams);
        String newSign = Objects.requireNonNull(Md5Utils.SHA(buildParams)).toUpperCase();
        return newSign.equals(sign);
    }

}
