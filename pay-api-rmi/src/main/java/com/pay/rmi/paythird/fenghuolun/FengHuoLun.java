package com.pay.rmi.paythird.fenghuolun;

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
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 风火轮
 */
@Service(FengHuoLun.channelNo)
public class FengHuoLun extends AbstractPay {

    static final String channelNo = "fenghuolun";

    private static final String payUrl = "http://203.107.46.205/fhl-restful/server/create/pay/order";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public FengHuoLun() {
        payTypeMap.put(OutChannel.wechatpay.name(), "2");
        payTypeMap.put(OutChannel.alipay.name(), "1");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "风火轮支付请求：{}", JSON.toJSONString(reqParams));

        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        //String result = restTemplate.postForObject(payUrl, HttpsParams.buildJsonEntity(params), String.class);
        HanYinFuApi http = new HanYinFuApi(payUrl, HanYinFuApi.POST);
        String string = JUtil.toJsonString2(params);
        String result = http.post(string);
        LogByMDC.info(channelNo, "风火轮支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);

        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String code = resultMap.get("result");
        Assert.isTrue("0".equals(code), "风火轮支付状态响应:" + resultMap.get("message"));

        String data = resultMap.get("datas");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });

        String qrCode = resultData.get("payPic");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrCode);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "风火轮支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("merchantCode", upMerchantNo);
        params.put("payPrice", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));
        params.put("merchantOrderNo", orderNo);
        params.put("merchantName", reqParams.getProduct());
        params.put("payType", payType);
        params.put("callbackUrl", getCallbackUrl(channelNo, merchNo, orderNo));

        LogByMDC.info(channelNo, "风火轮支付参与加签内容：{}", params);
        String sign = getSignStr(params, upPublicKey);
        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "风火轮支付回调内容：{}", params);
        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "风火轮支付回调订单：{}，重复回调", order.getOrderNo());
            return "ok";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "风火轮支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String tradeNo = params.get("merchantOrderNo");
        String tradeStatus = params.get("noticeType");
        String amount = params.get("payPrice");

        if (!"1".equals(tradeStatus)) {
            LogByMDC.error(channelNo, "风火轮支付回调订单：{}，支付未成功，不再向下通知", tradeNo);
            return "ok";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));
        order.setBusinessNo(tradeNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "风火轮支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "风火轮支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        System.out.println("风火轮支付下发通知成功.........................");
        return "ok";
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = "merchantName=" + params.get("merchantName") +
                "&merchantCode=" + params.get("merchantCode") +
                "&merchantOrderNo=" + params.get("merchantOrderNo") +
                "&payPrice=" + params.get("payPrice") +
                "&payType=" + params.get("payType") + "&" + upPublicKey;
        LogByMDC.info(channelNo, "风火轮支付参与MD5加签内容：{}", signParams);
        return Objects.requireNonNull(Md5Utils.MD5(signParams));
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");

        String merchantName = params.get("merchantName");
        try {
            merchantName = URLEncoder.encode(merchantName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RException("风火轮支付回调订单商品名称urlencode错误");
        }
        String signParams = "merchantName=" + merchantName +
                "&merchantOrderNo=" + params.get("merchantOrderNo") +
                "&noticeType=" + params.get("noticeType") +
                "&payPrice=" + params.get("payPrice") +
                "&payType=" + params.get("payType");
        String buildParams = signParams + "&" + upMerchantKey;
        LogByMDC.info(channelNo, "风火轮支付回调订单:{}，参与验签参数:{}", params.get("merchantOrderNo"), buildParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams));
        return newSign.equals(sign);
    }

}
