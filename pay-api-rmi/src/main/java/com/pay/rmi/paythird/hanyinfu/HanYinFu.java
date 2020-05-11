package com.pay.rmi.paythird.hanyinfu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.enums.OrderStatus;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.MatchUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.pay.rmi.paythird.hanyinfu.util.*;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


@Service(HanYinFu.channelNo)
public class HanYinFu extends AbstractPay {

    static final String channelNo = "hanyinfu";

    private static final String payUrl = "http://47.75.90.46:8880/webwt/pay/gateway.do";

    private static final String rsaPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCqZHtUGsyDwm3AX5Nfdio+pRoOVZmmZZKuBdiltvkKLLb9H7EsggvJs0n5yvRji/0irxZGi+jt3uaxERvA1JzFYvL7DVUOPbkoXGkdVS+fudQJsJulgvis1Q3XI2q9NYsNE/LGo/irvvuO38pBbyPf582X+TyuEC1mPoZl6St0HsSh4MEvK0qHMKIDBXqVsQotdKMZgXTaXKMnYR33magL4sBYEo/5RKZdKOYOLL3TOI0iogCkyNH4+wyRBi94kOD5UHHtWmsYo00aCh4mxf1win5kpdm9/7ErlvdSV/AS91msaJBTGf5+gKPBe26aSYCnYIy3clIuCnVYQ3YlcKr3AgMBAAECggEAXcRX/guVWjjHruKpthHP45N1yEeg+2nQE0YSV/deCxRaSueC14xlMkUgw37FiHaEAe86Ie5ia2yLpQtWK4KVFyaBslg+40xiNWzNR6AWSh64KfDvZmCxumKwcCEEX0U01SiSJHAdq125D4HlyqZ/pnror+YvV1Z9tVNZIIH1MLvLzJsn7WRl+U4iGdlBrNbZdVQIntfMd4wOoLWdc+56xkFUfZNna4HbHXLM4ad8A6KO8F4qzCUX/txNzdFtOepSVcCHAERwvUGoTR+6GM9LBHg3kgc46zOzwQPhbhJPyubkjt5R1RJOm4vvF3UnKipwJF3RO+IkaJOzh3YFnkX+UQKBgQDZH+o8nzIZtKjbfMc4+jqGKlTaDeV5iRyqxYQ6/IQFZnqtkYsRmKb/MOKtc/D6+l0IcIAzw2SNUjIJDTbou1sVbqr8eK9zcdXFa+buizTA5pVtHNTQGruHk9Qi6KnP2G/hQDkEE3y6MKg2CicCAesdj95zr0pVGHN5nymY/BJgmQKBgQDI5o9wkAci4ij5kX+guxio769/bggCKi8q1q9SUEzOLLwa0YvsS42gtEkLO+pyBmrZonGtHtXVMcSHrVq3BHrwE3PmeBmsHurZXnOoY3IImjQg41AyZncHd1pTUtUlbE3+A6LE/U7jxLA2U0qx80sn1i7Q+qyC/uN+vDElUExSDwKBgQC3I8tgzOrmcdMIJ6ynK7GXnzcpIhWQOQ3cIXRSiNIW+AS8SIpkEj2x/JsGsUfveqRkTPhmMQasiUs5BVNuZTID1vAUuvtKxhcJSeVlCjg6CYS6n1oGGrZZOmDx/QvXC/n6pkAxZvqK1iXcRx31/IZDzilVrOJm+pPUX2Mn7l7V6QKBgA5Jv4fc5nUHqqxdObt/svhI261vcOhega5FSIDNLzk2m0Y5Av+SPAdi6xL+duUXKcBCulz6pXCdMoIHxTzg97FdYM/SWNkR9dfxzL38HJ8aAH0aTZjYCr0gMu9cI8aelklrjsb90P+H/JRQhi5zxDOYGwpH2b8TS7C2x7QEqvjnAoGAcOwT9yVCULws1uIozHKSEMd4A9ZKn/nE6Te6Y6rxHnUy2wlal/5wDR2MRNuzdLj9eFZC7NJ9d4kykmvG/01Yc7iqNRvhhSerSDmljyDswhU7ZieE0xQWUe8yB6O7z0PdKa6cQBwVXdjeiRVh9t4r0pxb5z2bVrr1tAE9XHF8ZU4=";

    private static final String rsaPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtCMSCREYj7YYbKz8suAzHanEqqCzH6Mn4ooCSTkI1rvOZw5wp4hYD3HVOALRU5Lpzx4l3qMDlHVs6F/o0EfH9hKxYOC3P3W4XTN93LxOJjJUv8qBmNK2q476BfZ69htXZpp1vH24Vc9No4LrTfH4UsnqmXEXwE24wQBBwBk+Wc4yGTHKG5yABavLB2RCJiCK0tMFyWKxFAnEV1f8zomfeyTvwdaXh4LrPVzudxTJu3X+fP8Ac59b2xgFyBbtEbl01RLvCg+MAgRFCcAZBKfcWHl5rUDyLG/bei1bbtMQa+hg/ezZuFPA3ZzWXBIwRhwllAHTq7xl38Pax2IhZN5owwIDAQAB";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public HanYinFu() {
        payTypeMap.put(OutChannel.alipay.name(), "ALIPAY");
        payTypeMap.put(OutChannel.wechatpay.name(), "WXPAY");
        payTypeMap.put(OutChannel.qqpay.name(), "QQPAY");
        payTypeMap.put(OutChannel.wechath5.name(), "WXWAPPAY");
        payTypeMap.put(OutChannel.unionsm.name(), "CPPAY");
        payTypeMap.put(OutChannel.jdpay.name(), "JDPAY");
        payTypeMap.put(OutChannel.jdwap.name(), "JDWAPPAY");
        payTypeMap.put(OutChannel.alih5.name(), "ALIWAPPAY");

        payTypeMap.put(OutChannel.quickpay.name(), "quick");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "翰银付支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        Map<String, Object> requestMap = new HashMap<>();
        Map<String, Object> signMap = new HashMap<>();
        signMap.put("sign", params.get("sign"));
        requestMap.put("REQ_HEAD", signMap);
        params.remove("sign");
        requestMap.put("REQ_BODY", params);
        LogByMDC.info(channelNo, "翰银付支付响应 订单：{}，request：{}", reqParams.getOrderNo());
        //签名过期，等待加QQ群调试
        HanYinFuApi http = new HanYinFuApi(payUrl, HanYinFuApi.POST);
        String string = JUtil.toJsonString(requestMap);
        String result = http.post(string);
        LogByMDC.info(channelNo, "翰银付支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String repBody = resultMap.get("REP_BODY");
        String repHead = resultMap.get("REP_HEAD");

        Map<String, String> resultBody = JSON.parseObject(repBody, new TypeReference<Map<String, String>>() {
        });
        Map<String, String> resultHead = JSON.parseObject(repHead, new TypeReference<Map<String, String>>() {
        });
        LogByMDC.info(channelNo, "翰银付支付响应 订单：{}，结果体：{}", reqParams.getOrderNo(), resultBody);
        LogByMDC.info(channelNo, "翰银付支付响应 订单：{}，结果头：{}", reqParams.getOrderNo(), resultHead);
        String _sign = resultHead.get("sign");
        Assert.isTrue(!StringUtils.isEmpty(_sign), "翰银付支付状态响应失败，" + resultBody + ",resultHead:" + resultHead);

        String upPublicKey = mcpConfig.getUpKey();
        String vsign = HanYinFuApi.getSign(resultBody, upPublicKey);
        boolean verify = SecurityUtil.verify(vsign, _sign, rsaPublicKey, true);
        Assert.isTrue(verify, "翰银付支付状态响应验签失败");

        String rspcode = resultBody.get("rspcode");
        String orderState = resultBody.get("orderState");
        Assert.isTrue("000000".equals(rspcode), "翰银付支付失败,上游响应:" + resultBody.get("rspmsg"));
        Assert.isTrue("00".equals(orderState), "翰银付支付失败,上游响应:" + resultBody.get("rspmsg"));

        String payType = payTypeMap.get(reqParams.getOutChannel());
        String cardQrcode = "";
        if ("quick".equals(payType)) {
            cardQrcode = resultBody.get("payUrl");
        } else {
            cardQrcode = resultBody.get("qrcode");
        }
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(cardQrcode);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        LogByMDC.info(channelNo, "翰银付支付upMerchantNo：{}", upMerchantNo);
        String upPublicKey = mcpConfig.getUpKey();
        LogByMDC.info(channelNo, "翰银付支付upPublicKey：{}", upPublicKey);
        Assert.notNull(payType, "翰银付支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();
        Map<String, String> params = new TreeMap<>();
        if ("quick".equals(payType)) {
            params.put("tranCode", "1201");
            params.put("callBackUrl","http://www.google.com");
        } else {
            params.put("tranCode", "1101");
            params.put("termIp", reqParams.getReqIp());
            params.put("goodsDetail", HexStringUtils.toHex(reqParams.getMemo()));
        }
        params.put("agtId", upMerchantNo);
        params.put("orderAmt", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));
        params.put("orderId", orderNo);
        params.put("goodsName", HexStringUtils.toHex(reqParams.getProduct()));
        params.put("nonceStr", MatchUtils.generateShortUuid());
        params.put("notifyUrl", getCallbackUrl(channelNo, merchNo, orderNo));

        params.put("stlType", "T0");
        params.put("uId", "487291");
        params.put("payChannel", payType);

        LogByMDC.info(channelNo, "翰银付支付参与加签内容：{}", params);
        String sign = HanYinFuApi.getSign(params, upPublicKey);
        params.put("sign", RSASignature.signToHanYinFu(sign, rsaPrivateKey, true));
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "翰银付支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "翰银付支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String repBody = params.get("REP_BODY");
        String repHead = params.get("REP_HEAD");

        Map<String, String> resultBody = JSON.parseObject(repBody, new TypeReference<Map<String, String>>() {
        });
        LogByMDC.info(channelNo, "翰银付支付回调body内容：{}", resultBody);
        Map<String, String> resultHead = JSON.parseObject(repHead, new TypeReference<Map<String, String>>() {
        });
        LogByMDC.info(channelNo, "翰银付支付回调head内容：{}", resultHead);

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        String vsign = HanYinFuApi.getSign(resultBody, upMerchantKey);
        String _sign = resultHead.get("sign");
        boolean verify = SecurityUtil.verify(vsign, _sign, rsaPublicKey, true);
        if (!verify) {
            LogByMDC.error(channelNo, "翰银付支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String trade_no = resultBody.get("orderId");
        String trade_status = resultBody.get("orderState");
        String amount = resultBody.get("orderAmt");

        if (!"01".equals(trade_status)) {
            LogByMDC.error(channelNo, "翰银付支付回调订单：{}，支付未成功，不再向下通知", trade_no);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "翰银付支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "翰银付支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return "success";
    }

}
