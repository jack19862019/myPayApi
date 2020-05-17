package com.pay.rmi.paythird.bkzhifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.*;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 大万通
 */
@Service(BkZhiFu.channelNo)
public class BkZhiFu extends AbstractPay {

    static final String channelNo = "bkzhifu";

    private static final String payUrl = "http://47.244.112.68/api/pay/create";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public BkZhiFu() {
        payTypeMap.put(OutChannel.alih5.name(), "1A");//支付宝H5
        payTypeMap.put(OutChannel.wechatyssm.name(), "1B");//微信扫码
        payTypeMap.put(OutChannel.wechath5.name(), "1D");//微信H5

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) throws UnsupportedEncodingException, JsonProcessingException {
        LogByMDC.info(channelNo, "BK支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "BK支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {});
        String status = resultMap.get("code");
        Assert.isTrue("0".equals(status), "BK上游支付状态响应:" + resultMap.get("msg"));
        String qrCode = resultMap.get("payurl");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        if (OutChannel.wechatyssm.name().equals(reqParams.getOutChannel()) || OutChannel.wechath5.name().equals(reqParams.getOutChannel())) {//微信扫码或微信H5
            String img = BuildFormUtils.buildSubmitForm2("data:image/png;base64," + QRCodeUtils.generate(qrCode));
            orderApiRespParams.setQrcode_url(img);
        }else {
            orderApiRespParams.setCode_url(qrCode);
        }

        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "BK支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        String stamp = System.currentTimeMillis() + "";
        stamp = stamp.substring(0, 10);

        Map<String, String> params = new TreeMap<>();
        params.put("coopId", upMerchantNo);//商户编号
        params.put("outOrderNo", orderNo);//商户订单号
        params.put("subject", "apple");//商品描述
        params.put("money", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));//交易金额
        params.put("notifyUrl", getCallbackUrl(channelNo, merchNo, orderNo));//异步通知地址
        params.put("pathType", payType);//支付类型
        LogByMDC.info(channelNo, "BK支付参与加签内容：{}", params);
        String sign = getSignStr(params, upPublicKey);
        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) throws UnsupportedEncodingException {
        LogByMDC.info(channelNo, "BK支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "BK支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String tradeNo = params.get("outOrderNo");
        String tradeStatus = params.get("code");
        String amount = params.get("money");

        if (!"0".equals(tradeStatus)) {
            LogByMDC.error(channelNo, "BK支付回调订单：{}，支付未成功，不再向下通知", tradeNo);
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).divide(new BigDecimal("100")));
        order.setBusinessNo(tradeNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "BK支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "BK支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        System.out.println("BK支付下发通知成.........................");
        return "success";
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + upPublicKey;
        LogByMDC.info(channelNo, "BK支付参与MD5加签内容：{}", signParams);
        return Objects.requireNonNull(Md5Utils.MD5(signParams));
    }

}
