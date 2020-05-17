package com.pay.rmi.paythird.niubohu;

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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 牛博户(5g支付)
 */
@Service(NiuBoHu.channelNo)
public class NiuBoHu extends AbstractPay {
    static final String channelNo = "niubohu";
    //static final String payUrl = "http://47.52.199.87/Pay_Index.html";
    static final String payUrl = "http://47.56.122.108/Pay_Index.html";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public NiuBoHu() {
        payTypeMap.put(OutChannel.alipay.name(), "904");
        payTypeMap.put(OutChannel.wechatpay.name(), "901");
        payTypeMap.put(OutChannel.alih5.name(), "911");
        payTypeMap.put(OutChannel.aliwap.name(), "915");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "牛博户支付，请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        Map<String, String> dataMap = JSON.parseObject(result, new TypeReference<TreeMap<String, String>>() {
        });

        String code = dataMap.get("code");
        Assert.isTrue("200".equals(code), "牛博户支付状态响应:" + dataMap.get("msg"));

        String qrCode = dataMap.get("data");
        Assert.isTrue(!StringUtils.isEmpty(qrCode), "牛博户支付响应未返回支付路徑");

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrCode);
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("pay_memberid", upMerchantNo);
        params.put("pay_orderid", orderNo);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        params.put("pay_applydate", df.format(new Date()));
        params.put("pay_bankcode", payType);
        params.put("pay_notifyurl", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("pay_callbackurl", reqParams.getReturnUrl());
        params.put("pay_amount", amount);
        LogByMDC.info(channelNo, "牛博户支付,参数组装：{}", params);
        String md5Value = Objects.requireNonNull(Md5Utils.MD5(getSign(params, upPublicKey))).toUpperCase();

        params.put("pay_productname", reqParams.getProduct());
        params.put("pay_username", "K8-" + orderNo);
        params.put("format", "json");

        params.put("pay_md5sign", md5Value);
        return params;
    }

    private String getSign(Map<String, String> params, String key) {
        return SignUtils.buildParams(params) + "&key=" + key;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "牛博户支付回调内容：{}", params);

        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "牛博户支付回调订单：{}，重复回调", order.getOrderNo());
            return "ok";
        }
        String upPublicKey = mcpConfig.getUpKey();

        boolean signVerify = verifySignParams(params, upPublicKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "牛博户支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String amount = params.get("amount");
        String orderid = params.get("orderid");
        String returncode = params.get("returncode");
        if (!"00".equals(returncode)) {
            LogByMDC.error(channelNo, "牛博户支付回调订单：{}，支付未成功，不再向下通知", order.getOrderNo());
            return "ok";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(orderid);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "牛博户支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "牛博户支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
        }
        return "ok";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params);
        LogByMDC.info(channelNo, "牛牛支付回调订单:{}，参与验签参数:{}", params.get("orderid"), signParams);
        String buildParams = signParams + "&key=" + upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        return newSign.equals(sign);
    }
}
