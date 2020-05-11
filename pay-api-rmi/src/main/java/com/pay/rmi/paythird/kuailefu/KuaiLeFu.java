package com.pay.rmi.paythird.kuailefu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mysema.commons.lang.URLEncoder;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 快乐付
 */
@Service(KuaiLeFu.channelNo)
public class KuaiLeFu extends AbstractPay {

    static final String channelNo = "kuailefu";

    private static final String payUrl = "http://khgri4829.com:6084/api/pay/V2";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public KuaiLeFu() {
        payTypeMap.put(OutChannel.onlinepay.name(), "0");
        payTypeMap.put(OutChannel.unionpay.name(), "1");
        payTypeMap.put(OutChannel.alipay.name(), "2");
        payTypeMap.put(OutChannel.alipayzkh5.name(), "7");
        payTypeMap.put(OutChannel.alih5.name(), "8");

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "快乐付支付请求：{}", JSON.toJSONString(reqParams));

        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        Map<String, String> head = new HashMap<String, String>();
        head.put("Content-Type", "application/json");
        String result = HttpKit.post(payUrl, JSON.toJSONString(params), head);
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

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new HashMap<String, String>();
        params.put("merchantNo", upMerchantNo);
        params.put("version", "V2");
        params.put("signType", "MD5");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("date", formatter.format(new Date()));
        params.put("channleType", payType);
        params.put("orderNo", orderNo);
        params.put("bizAmt", new BigDecimal(amount) + "");
        params.put("noticeUrl", getCallbackUrl(channelNo, merchNo, orderNo));
        //params.put("bankCode",  "ABC");
        params.put("accName", "张三");
        params.put("cardNo", "6230520080090842211");

        String signStr = formatSignData(params);
        System.out.println("signStr签名：" + signStr);

        String sign = PayMD5.MD5Encode(signStr + upPublicKey).toLowerCase();
        params.put("sign", sign);
        return params;
    }

    public static String formatSignData(Map<String, String> signDataMap) {
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
//		log.debug("To be signed data: {}", s);
        return s;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + upPublicKey;
        LogByMDC.info(channelNo, "BK支付参与MD5加签内容：{}", signParams);
        return Objects.requireNonNull(Md5Utils.MD5(signParams));
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "快乐付支付回调内容：{}", params);
        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "快乐付支付回调订单：{}，重复回调", order.getOrderNo());
            return "SUCCESS";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "快乐付支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "FAIL";
        }
        String trade_no = params.get("orderNo");
        String trade_status = params.get("status");
        String amount = params.get("bizAmt");


        if (!"1".equals(trade_status)) {
            LogByMDC.error(channelNo, "快乐付支付回调订单：{}，支付未成功{}，不再向下通知", trade_no);
            return "SUCCESS";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "快乐付支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "快乐付支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return "SUCCESS";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        Map<String, String> treeMap = new TreeMap<>(params);
        String sign = treeMap.remove("sign");

        String signStr = formatSignData(params);
        System.out.println("signStr签名：" + signStr);

        String newSign = PayMD5.MD5Encode(signStr + upMerchantKey).toLowerCase();

        return newSign.equals(sign);
    }

}
