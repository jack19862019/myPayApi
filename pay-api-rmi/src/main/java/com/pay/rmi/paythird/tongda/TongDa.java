package com.pay.rmi.paythird.tongda;

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
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.HttpsParams;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.pay.rmi.paythird.xinyangyang.Md5;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service(TongDa.channelNo)
public class TongDa extends AbstractPay {
    public static final String channelNo = "tongda";

    static final String payUrl = "http://pay.pay138.vip/api/pay/create_order";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public TongDa() {
        payTypeMap.put(OutChannel.wechath5.name(), "8003");//微信pdd
        payTypeMap.put(OutChannel.wechatpay.name(), "8045");//八方支付微信h5

        payTypeMap.put(OutChannel.alipayd1.name(), "8044");//支付宝PDD-H5
        payTypeMap.put(OutChannel.alipdd.name(), "8028");//支付宝pdd
        payTypeMap.put(OutChannel.alih5.name(), "8035");//支付宝h5
        payTypeMap.put(OutChannel.aliysh5.name(), "8040");//支付宝原生h5
        payTypeMap.put(OutChannel.alih5d1.name(), "8037");//支付宝h5 pdd

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "通达支付，请求：{}", JSON.toJSONString(reqParams));

        //根据入参加工参数成通汇宝支付需要的参数与签名
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "通达支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("retCode");
        Assert.isTrue("SUCCESS".equals(code), "通达支付状态响应:" + resultMap.get("retMsg"));
        String data = resultMap.get("payParams");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        String payUrl = resultData.get("payUrl");

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setPay_form(payUrl);
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "通达支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("mchId", upMerchantNo);//商户号
        params.put("appId", "53288e89cdbc44ac8d3bf9235fc42126");//应用id
        params.put("productId", payType);//支付类型
        params.put("mchOrderNo", orderNo);//商户订单编号
        params.put("currency", "cny");//币种
        params.put("amount", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));

        params.put("notifyUrl", getCallbackUrl(channelNo, merchNo, orderNo));

        params.put("clientIp", reqParams.getReqIp());
        params.put("device", "ios10.3.1");

        params.put("subject", "subject");
        params.put("body", "body");
        params.put("extra", "1");

        String buildParams = SignUtils.buildParams(params) + "&key=" + upPublicKey;
        String sign = Objects.requireNonNull(Md5Utils.MD5(buildParams).toUpperCase());

        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "通达支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "通达支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "通达支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String amount = params.get("amount");
        String systemNo = params.get("payOrderId");
        String paystatus = params.get("status");
        if (!"2".equals(paystatus)) {
            LogByMDC.error(channelNo, "通达支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "通达支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "通达支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params);
        LogByMDC.info(channelNo, "通达支付回调订单:{}，参与验签参数:{}", params.get("out_trade_no"), signParams);
        String buildParams = signParams + "&key=" + upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        return newSign.equals(sign);
    }
}
