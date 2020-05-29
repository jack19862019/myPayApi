package com.pay.rmi.paythird.kuailefu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mysema.commons.lang.URLEncoder;
import com.pay.common.enums.OrderStatus;
import com.pay.common.exception.Assert;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.entity.UpPayTypeEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 快乐付
 */
@Service(KuaiLeFu.channelNo)
public class KuaiLeFu extends AbstractPay {

    static final String channelNo = "kuailefu";

    @Override
    public OrderApiRespParams order(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        initReqOrder(channel, mcpConfig, reqParams);
        Map<String, String> map = requestToUpParams();
        String result = httpRequestToUp(channel.getUpPayUrl(), map);
        return returnRespToDown(result);
    }

    @Override
    public String callback(OrderEntity order,  McpConfigEntity mcpConfig ,Map<String, String> params) {
        initCallBack(order,mcpConfig);
        if (order.getOrderStatus() == OrderStatus.succ) {
            return "SUCCESS";
        }
        boolean signVerify = verifySignParams(params);
        Assert.mustBeTrue(signVerify,"验签失败！");
        return updateOrder(params);
    }

    @Override
    protected Map<String, String> requestToUpParams() {

        Optional<UpPayTypeEntity> upPayTypeEntity = channelEntity.getUpPayTypes().stream()
                .filter(e -> e.getPayType().getPayTypeFlag().equals(reqParams.getOutChannel())).findFirst();
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "快乐付不支持的支付方式:" + reqParams.getOutChannel());

        Map<String, String> params = new HashMap<String, String>();
        params.put("merchantNo", mcpConfig.getUpMerchantNo());
        params.put("version", "V2");
        params.put("signType", "MD5");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("date", formatter.format(new Date()));
        params.put("channleType", upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("orderNo", reqParams.getOrderNo());
        params.put("bizAmt", new BigDecimal(reqParams.getAmount()) + "");
        params.put("noticeUrl", getCallbackUrl(channelNo, reqParams.getMerchNo(), reqParams.getOrderNo()));
        params.put("accName", "张三");
        params.put("cardNo", "6230520080090842211");

        String signStr = formatSignData(params);
        params.put("sign", signToUp(signStr));
        return params;
    }

    @Override
    protected String signToUp(String context) {
        return PayMD5.MD5Encode(context + mcpConfig.getUpKey()).toLowerCase();
    }

    @Override
    protected String httpRequestToUp(String payUrl, Map<String, String> requestToUpParams) {
        Map<String, String> head = new HashMap();
        head.put("Content-Type", "application/json");
        return  HttpKit.post(payUrl, JSON.toJSONString(requestToUpParams), head);
    }

    @Override
    protected OrderApiRespParams returnRespToDown(String result) {
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

    @Override
    protected boolean verifySignParams(Map<String, String> params) {
        Map<String, String> treeMap = new TreeMap<>(params);
        String sign = treeMap.remove("sign");
        String signStr = formatSignData(params);
        String newSign = PayMD5.MD5Encode(signStr + mcpConfig.getUpKey()).toLowerCase();
        return newSign.equals(sign);
    }

    @Override
    protected String updateOrder(Map<String, String> params) {
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
        return"SUCCESS";
    }


    private static String formatSignData(Map<String, String> signDataMap) {
        Set<String> sortedSet = new TreeSet<String>(signDataMap.keySet());
        StringBuffer sb = new StringBuffer();
        for (String key : sortedSet) {
            if ("sign".equalsIgnoreCase(key)) {
                continue;
            }

            if (signDataMap.get(key) != null) {
                String v = String.valueOf(signDataMap.get(key));
                if (StringUtils.isNotBlank(v)) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(v);
                    sb.append("&");
                }
            }
        }
        String s = sb.toString();
        if (s.length() > 0) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
