package com.pay.rmi.paythird.kuailefu;

import com.pay.common.enums.OrderStatus;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.paythird.AbstractPay;
import com.pay.rmi.paythird.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 快乐付
 */
@Service(KuaiLeFu.channelNo)
public class KuaiLeFu extends AbstractPay implements PayService {

    static final String channelNo = "kuailefu";

    @Autowired
    ReqParamsBuilder reqParamsBuilder;

    @Autowired
    SignBuilder signBuilder;

    @Autowired
    HttpReqHelper httpReqHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String callbackUrl = getCallbackUrl(channelNo, reqParams.getMerchNo(), reqParams.getOrderNo());
        Map<String, String> map = reqParamsBuilder.requestToUpParams(channel, mcpConfig, reqParams, callbackUrl);
        String signStr = SignBuilder.formatSignData(map);
        String sign = signBuilder.signToUp(signStr, mcpConfig.getUpKey());
        map.put("sign", sign);
        String result = httpReqHelper.httpRequestToUp(channel.getUpPayUrl(), map);
        return null;//returnRespToDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        if (order.getOrderStatus() == OrderStatus.succ) {
            return "SUCCESS";
        }
        /*boolean signVerify = verifySignParams(params);
        Assert.mustBeTrue(signVerify, "验签失败！");
        return updateOrder(params);*/
        return null;
    }

    /*public OrderApiRespParams returnRespToDown(String result) {
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("detail");
        Map<String, String> resultMap1 = JSON.parseObject(code, new TypeReference<Map<String, String>>() {
        });
        LogByMDC.info(channelNo, "快乐付支付请求响应：{}", JSON.toJSONString(resultMap1));
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        if (resultMap1.get("PayURL") != null && !resultMap1.get("PayURL").equals("")) {
            StringBuffer sb = new StringBuffer();
            String payURL = resultMap1.get("PayURL");
            String domain = payURL.split("\\?")[0];
            String url = payURL.split("\\?")[1];
            StringBuffer append = sb.append(domain).append("?").append(URLEncoder.encodeURL(url));
            com.pay.common.exception.Assert.isEmpty("快乐付返回支付地址为空", append);
            LogByMDC.info(channelNo, "快乐付支付请求响应code：{}", append);
            orderApiRespParams.setCode_url(append.toString());
        }
        if (resultMap1.get("PayHtml") != null && !resultMap1.get("PayHtml").equals("")) {
            LogByMDC.info(channelNo, "快乐付支付请求响应html：{}", resultMap1.get("PayHtml"));
            orderApiRespParams.setPay_form(resultMap1.get("PayHtml"));
        }
        return orderApiRespParams;
    }

    public boolean verifySignParams(Map<String, String> params) {
        Map<String, String> treeMap = new TreeMap<>(params);
        String sign = treeMap.remove("sign");
        String signStr = formatSignData(params);
        String newSign = PayMD5.MD5Encode(signStr + mcpConfig.getUpKey()).toLowerCase();
        return newSign.equals(sign);
    }

    public String updateOrder(Map<String, String> params) {
        String trade_no = params.get("orderNo");
        String trade_status = params.get("status");
        String amount = params.get("bizAmt");

        if (!"1".equals(trade_status)) {
            return "SUCCESS";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
        } catch (Exception e) {
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return "SUCCESS";
    }*/
}
