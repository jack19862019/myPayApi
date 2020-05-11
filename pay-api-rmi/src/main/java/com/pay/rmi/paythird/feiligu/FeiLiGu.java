package com.pay.rmi.paythird.feiligu;

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
import com.pay.rmi.common.utils.*;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import sun.security.krb5.internal.PAData;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 快银
 */
@Service(FeiLiGu.channelNo)
public class FeiLiGu extends AbstractPay {

    static final String channelNo = "feiligu";

    private static final String payUrl = "http://api.feiligu.cn/pay";

    private static final String rsaPrivateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCW4T0w+lsY53ulVF7O3Y8hMSo7oHfeJhYahxB37fi8MSRKXRNmY2MljaWBYWhKfxbpovdB2S4oAuXmsaBHvI66Dc35LQiYEBJ5LIZkOB7NAyoUDN5LKUnvRvI4/oy2+a8njRRYCCIYeXhpwJyseRuIwLgQmN6Z1h90OJS5GrME0Ec6Ou64KL/bAutNaIK+hzr7GvMHAdopvyW6iywmXN9chCC8qFFZWNBXpxriNZF2wsDneziLSN1TNOu+GzbHPMp6824B46SOKp0Vs13U3XhHuUDmDsJdBMtSBjAtA6XF640W5564nn4cUs8ts0zYrcSmT63O5AbKpTF0tFXq+nbdAgMBAAECggEAd9whP2pzuhoS2Olok8/g545mLY4yC3GYN6S6iXusENy9JrJAOiOUXP3k0B5wulWx/xLueAovJ6v9tUHAwZxiih1zVpLW8+44rriXXsBSf60W3WzHn1ACoypqPKrDYIyD/9iMo856P5Un+mMAU+e13vKyhsTaeb1nqW1VgKNcOeJ6tDohTMMyxfLGEPFbMpA6WpA070yIGUMHgVfZBX2Sb0VJeHcBH5JMtD8BHSfDCtocOZILQOWNsqdt4bEQrHmZLIU5O8QArIOBGnmWNK9oNiTTiyXn2VLSmXLVI8F0QpYepuEbqpGsdOKCJogPsnZleyqLO721xvA4yaPCasZJoQKBgQDqrEE93RNLt+vMdE59G3/Vkeyh021VcMIw/nYhZT3byaGJuVQXE0+wpw3Tu5LD5YV4AnWqtUzDXL7O+FEY0gsHLw1JoU3u+NUVw20STb/oatW9Saxe9tClafo8kK+DFqHbJ+TyRl3NFEP7JzjnrQW2/x8hCQ+0cGmrdSp3CAj+2QKBgQCkl4Kt4GWSER2U3sJZXa4/yT0wZFiJl3FhOWSsR7RoAHWnAnYVgqbdFvW5EekwyrM59T3whJFr7NkISYyF/JQcpljyvnJQUA5Q2sP5J6WGl2PVMiNOew3Mwfnpp8/ne351oU0s1Xk8/9x7TjaCIToOwbUiFan81y+1fQkXTEi9pQKBgQCRpAAFhcuw5V9JDzGbAVMP17V1JUMokIdkYrgrt6pxWiyPzd37SDff2X2CV1oZi7uOpHAkVspiQJAG4SNrdQf1GGV1uc8lLXcJP7TUdD5S3uLxy+RXN3R8BE6SAfV7NLOU0KiNF9ClMxuRpmDxn3b5gFHwbbA6er2J9Hg7UVhL2QKBgAR11qS/oAEFVbfjEYE6eK5h1baqAXw5DmmEFKZWix0GZinQd0K996emJ6KMSbV4yMKZUsRh008JbBg9ZgF7hsf/+gkaAB8NDFZEl2r5GBM9pwbwBOTrkMQrvYoHcirjvdmSNXUxk8eDZ4Yv6Go+XERqG26BB8pCSpiwer6W4qHBAoGBAJrHPqa/HlQIaH4OcBrDJqp/05iby9LroH/NjFjLM1rINXMb8+8aI4CuKdleefBQTPx9p46fEV9ekVpfuYZH2oEDVfztCTXuNBRqQd1MzJP6hFV7qxKvQsaXUy+9YPLZaCzGlAb/qx0t80Ajm2aSpY4u9N1lnF43XQ+f1HWgEOm0";

    private static final String rsaPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAluE9MPpbGOd7pVRezt2PITEqO6B33iYWGocQd+34vDEkSl0TZmNjJY2lgWFoSn8W6aL3QdkuKALl5rGgR7yOug3N+S0ImBASeSyGZDgezQMqFAzeSylJ70byOP6MtvmvJ40UWAgiGHl4acCcrHkbiMC4EJjemdYfdDiUuRqzBNBHOjruuCi/2wLrTWiCvoc6+xrzBwHaKb8luossJlzfXIQgvKhRWVjQV6ca4jWRdsLA53s4i0jdUzTrvhs2xzzKevNuAeOkjiqdFbNd1N14R7lA5g7CXQTLUgYwLQOlxeuNFueeuJ5+HFLPLbNM2K3Epk+tzuQGyqUxdLRV6vp23QIDAQAB";

    //private static final String shPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhOFWFRDT/J0UwEPeSYORKRZuRbnkdj8GmHpJzFJkdGTL+Km4jSr+vB8K/v9K5aRTTCnfDCNTy0pE/tL6F7N0b3Zi9Zv7uGuqmpdAb2SzeAWaT5Q7FlzkCm3RMfi5NHkPgR3zoRs/EBV6hV6tt2ccK3mJYZE4rjvi0aNnrXTLza1XpJHc+qPQifR1EsGweraxKkwqQhvz8RR3jKCJ1eJZS46o3Uk+ijj3X5FV3yzeNYDsTdtwc0Ve+LasR9700DXrfWUcCuZpRFGvf0qgo2jf1Oe0oyxc68PbP03z3brAsZI7hGEen9W6hf8arhDr9X4OcbsaZ0Bv28CbbfmHlpG7pwIDAQAB";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public FeiLiGu() {
        payTypeMap.put(OutChannel.aliwap.name(), "Alipay_wap");
        payTypeMap.put(OutChannel.alipay.name(), "Alipay_QRcode");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "菲利谷支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        System.out.println("---------"+JSON.toJSONString(params));
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "菲利谷支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);

        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String code = resultMap.get("code");
        Assert.isTrue(!"0".equals(code), "菲利谷支付状态响应:" + resultMap.get("msg"));

        String data = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });

        String qrCode = resultData.get("payurl");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrCode);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "菲利谷支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("merId", upMerchantNo);
        params.put("orderAmt", amount);
        params.put("orderId", orderNo);
        params.put("desc", reqParams.getProduct());
        params.put("channel", payType);
        params.put("nonceStr", MatchUtils.generateShortUuid());
        params.put("ip", reqParams.getReqIp());
        params.put("returnUrl", reqParams.getReturnUrl());
        params.put("notifyUrl", getCallbackUrl(channelNo, merchNo, orderNo));

        LogByMDC.info(channelNo, "菲利谷支付参与加签内容：{}", params);
        String sign = getSignStr(params, upPublicKey);
        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "菲利谷支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "菲利谷支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "菲利谷支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String tradeNo = params.get("orderId");
        String tradeStatus = params.get("status");
        String amount = params.get("orderAmt");

        if (!"1".equals(tradeStatus)) {
            LogByMDC.error(channelNo, "菲利谷支付回调订单：{}，支付未成功，不再向下通知", tradeNo);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(tradeNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "菲利谷支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "菲利谷支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
        }
        System.out.println("菲利谷支付下发通知成.........................");
        return "success";
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "菲利谷支付参与MD5加签内容：{}", signParams);
        String md5Str = Objects.requireNonNull(Md5Utils.MD5(signParams)).toUpperCase();
        return SHA256WithRSAUtils.buildRSASignByPrivateKey(md5Str, rsaPrivateKey);
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params);
        String buildParams = signParams + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "菲利谷支付回调订单:{}，参与验签参数:{}", params.get("orderId"), buildParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        return SHA256WithRSAUtils.buildRSAverifyByPublicKey(newSign, rsaPublicKey, sign);
    }

}
