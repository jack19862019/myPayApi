package com.pay.rmi.paythird.wukong;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
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

@Service(WuKong.channelNo)
public class WuKong extends AbstractPay {

    static final String channelNo = "wukong";

    private static final String payUrl = "http://129.204.131.246:8100/api/sig/v1/";
    private final Map<String, String> payTypeMap = new HashMap<>();

    public WuKong() {
        payTypeMap.put(OutChannel.aliyssm.name(), "alipay/native");
        payTypeMap.put(OutChannel.aliwap.name(), "alipay/wap");
        payTypeMap.put(OutChannel.alipdd.name(), "alipay/pdd");
        payTypeMap.put(OutChannel.wechatwap.name(), "wx/wappay");
        payTypeMap.put(OutChannel.wechatyssm.name(), "wx/native");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "悟空支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        //发送支付请求
        String result = restTemplate.postForObject(payUrl + payTypeMap.get(reqParams.getOutChannel()), HttpsParams.buildFormEntityXml(params), String.class);
        LogByMDC.info(channelNo, "悟空支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = MapToXml.toMap(result);
        String status = resultMap.get("status");
        Assert.isTrue("0".equals(status), "悟空支付状态响应:" + resultMap.get("message"));
        String payUrl = resultMap.get("redirect_url");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(payUrl);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "悟空支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("version", "1.0");
        params.put("mch_id", upMerchantNo);
        params.put("out_trade_no", orderNo);
        params.put("body", reqParams.getProduct());
        params.put("total_fee", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));
        params.put("mch_create_ip", reqParams.getReqIp());

        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("nonce_str", MatchUtils.generateShortUuid());
        LogByMDC.info(channelNo, "悟空支付参与加签内容：{}", params);
        String sign = Objects.requireNonNull(Md5Utils.MD5(getSignStr(params, upPublicKey))).toUpperCase();
        params.put("sign", sign);
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParamsIgnoreNull(params) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "悟空支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "悟空支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "悟空支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String trade_no = params.get("out_trade_no");
        String amount = params.get("total_fee");

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "悟空支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "悟空支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
        }
        return "success";
    }

}
