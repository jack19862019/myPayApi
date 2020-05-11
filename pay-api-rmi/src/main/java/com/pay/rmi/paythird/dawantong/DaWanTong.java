package com.pay.rmi.paythird.dawantong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.HttpsParams;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;

import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 大万通
 */
@Service(DaWanTong.channelNo)
public class DaWanTong extends AbstractPay {

    static final String channelNo = "dawantong";

    private static final String payUrl = "https://wantong-pay.com/WTPay";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public DaWanTong() {
        payTypeMap.put(OutChannel.wechatpay.name(), "weixin");//微信
        payTypeMap.put(OutChannel.wechath5.name(), "weixin-h5");//微信H5
        payTypeMap.put(OutChannel.alipay.name(), "zhifubao");//支付宝
        payTypeMap.put(OutChannel.aliysh5.name(), "zhifubao-h5");//支付宝（原生）
        payTypeMap.put(OutChannel.qqpay.name(), "qq");//qq支付
        payTypeMap.put(OutChannel.unionwg.name(), "wangguan");//网关
        payTypeMap.put(OutChannel.unionquickpay.name(), "kuaijie");//快捷
        payTypeMap.put(OutChannel.unionsm.name(), "yl");//银联扫码
        payTypeMap.put(OutChannel.jdpay.name(), "jd");//京东

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) throws UnsupportedEncodingException, JsonProcessingException {
        LogByMDC.info(channelNo, "大万通支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("sign", URLEncoder.encode(params.get("sign"), "utf8"));
        requestParams.put("signtype", "MD5");
        params.remove("sign");
        ObjectMapper objectMapper = new ObjectMapper();//创建json对象
        requestParams.put("transdata", URLEncoder.encode(objectMapper.writeValueAsString(params), "utf8"));//把map转为json
        String result = restTemplate.postForObject(payUrl, requestParams, String.class);
        LogByMDC.info(channelNo, "大万通支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {});
        String status = resultMap.get("payment");
        Assert.isTrue("true".equals(status), "大万通上游支付状态响应:" + resultMap.get("message"));
        String qrCode = resultMap.get("payUrl");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrCode);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "大万通支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        String stamp = System.currentTimeMillis() + "";
        stamp = stamp.substring(0, 10);

        Map<String, String> params = new TreeMap<>();
        params.put("merchant_code", upMerchantNo);//商户编号
        params.put("appno_no", "Asun1579439213211");//应用编号
        params.put("order_no", orderNo);//商户订单号

        params.put("order_amount", new BigDecimal(amount).toString());//交易金额
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("order_time", formatter.format(new Date()));//交易时间
        params.put("product_name", reqParams.getProduct());//产品名称
        params.put("product_code", "222");//产品编号
        params.put("user_no", reqParams.getUserId());//用户编号（数字+字母）
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));//异步通知地址
        params.put("pay_type", payType);//支付类型
        params.put("bank_code", "ICBC");//银行编码
        params.put("return_url", reqParams.getReturnUrl());//成功回跳地址
        params.put("merchant_ip", reqParams.getReqIp());//用户IP地址
        params.put("bank_card", "6320156123546531215");//银行卡号
        LogByMDC.info(channelNo, "大万通支付参与加签内容：{}", params);
        String sign = getSignStr(params, upPublicKey);
        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) throws UnsupportedEncodingException {
        LogByMDC.info(channelNo, "大万通支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "大万通支付回调订单：{}，重复回调", order.getOrderNo());
            return "200";
        }
        String upMerchantKey = mcpConfig.getUpKey();
        String transdata = params.get("transdata");
        transdata = URLDecoder.decode(transdata);
        Map<String, Object> map = (Map) JSON.parse(transdata);//json字符串转化为map
        String sign = params.get("sign");//回调的签名

        String signParams = SignUtils.buildParamsObject(map);
        String buildParams = signParams + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "大万通支付回调订单:{}，参与验签参数:{}", params.get("orderid"), buildParams);
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        boolean signVerify = newSign.equals(sign);//回调验签
        if (!signVerify) {
            LogByMDC.error(channelNo, "大万通支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String tradeNo = map.get("order_no").toString();
        String tradeStatus = map.get("payment").toString();
        String amount = map.get("order_amount").toString();

        if (!"支付成功".equals(tradeStatus)) {
            LogByMDC.error(channelNo, "大万通支付回调订单：{}，支付未成功，不再向下通知", tradeNo);
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(tradeNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "大万通支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "大万通支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        System.out.println("大万通支付下发通知成.........................");
        return "200";
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "大万通支付参与MD5加签内容：{}", signParams);
        return Objects.requireNonNull(Md5Utils.MD5(signParams)).toUpperCase();
    }

}
