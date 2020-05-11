package com.pay.rmi.paythird.yjpay;

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
import com.pay.rmi.common.utils.HttpsParams;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service(Yjpay.channelNo)
public class Yjpay extends AbstractPay {

    public static final String channelNo = "yjpay";

    private final String payUrl = "http://api.itzoon.com/api/addOrder";

    private Map<String, String> payTypeMap = new HashMap<>();

    public Yjpay() {
        payTypeMap.put(OutChannel.alih5.name(), "wap");
        payTypeMap.put(OutChannel.alipay.name(), "qrcode");


        //近期上限中
        payTypeMap.put(OutChannel.wechath5.name(), "wxwap");
        payTypeMap.put(OutChannel.wechatpay.name(), "wxqrcode");
        payTypeMap.put(OutChannel.quickpay.name(), "ylkj");
        payTypeMap.put(OutChannel.unionwap.name(), "ylwg");
        payTypeMap.put(OutChannel.unionpay.name(), "ylsm");
        payTypeMap.put(OutChannel.qqpay.name(), "qqqb");
    }


    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "一加支付请求：{}", JSON.toJSONString(reqParams));
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "一加支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();
        String product = reqParams.getProduct();
        String returnUrl = reqParams.getReturnUrl();


        Map<String, String> params = new HashMap<>();
        //商户号，由平台分配
        params.put("merchant", upMerchantNo);
        //金额，单位为分
        params.put("amount", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));
        //支付产品类型
        params.put("pay_type", payType);
        //商户订单号
        params.put("order_no", orderNo);
        //下单时间，Unix时间戳秒
        params.put("order_time", String.valueOf(System.currentTimeMillis()));
        //商品描述
        params.put("subject", product);
        //异步回调地址
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));
        //同步回调地址
        params.put("callback_url", returnUrl);

        String strParams = SignUtils.buildParams(params);
        String sign = DigestUtils.sha1Hex(strParams + "&key=" + upPublicKey);

        params.put("sign", sign);

        LogByMDC.info(channelNo, "订单：{}，request：{}", orderNo, JSON.toJSONString(params));
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "订单：{}，response：{}", orderNo, result);

        params = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String code = params.get("code");
        String success = params.get("success");
        String error = params.get("error");
        if (!"0000".equals(code) || !"true".equals(success) || !"false".equals(error)) {
            String errMsg = params.get("errorMsg");
            String msg = params.get("msg");
            LogByMDC.error(channelNo, "订单：{}，上游返回：{}", orderNo, errMsg == null ? msg : errMsg);
            //return R.error("上游返回：" + (errMsg == null ? msg : errMsg));
        }

        result = params.get("result");
        Assert.notNull(result, "一加上游返回结果为null,请联系一加上游技术人员:" + result);
        params = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String upSign = params.get("sign");
        params.remove("sign");

        strParams = SignUtils.buildParams(params);
        sign = DigestUtils.sha1Hex(strParams + "&key=" + upPublicKey).toUpperCase();
        if (!sign.equals(upSign)) {
            LogByMDC.error(channelNo, "通道：{}，订单：{}，验证上游返回签名失败", channelNo, orderNo);
            //return R.error("验证上游返回签名失败");
        }

        String qrCode = params.get("qrCode");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrCode);
        return orderApiRespParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "回调内容：{}", params);

        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String orderNo = order.getOrderNo();
        String upMerchantKey = mcpConfig.getUpKey();

        String upSign = params.get("sign");
        params.remove("sign");

        String strParams = SignUtils.buildParams(params);
        String sign = DigestUtils.sha1Hex(strParams + "&key=" + upMerchantKey).toUpperCase();

        if (!sign.equalsIgnoreCase(upSign)) {
            LogByMDC.error(channelNo, "订单：{}，签名验证失败", orderNo);
            return "fail";
        }

        //平台订单号
        String tranNo = params.get("tranNo");
        //单位元
        String amount = params.get("amount");

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(tranNo);
        orderService.update(order);

        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "一加支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "一加支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return "success";
    }
}
