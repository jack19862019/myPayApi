package com.pay.rmi.paythird.lixiang;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service(LiXiang.channelNo)
public class LiXiang extends AbstractPay {
    public static final String channelNo = "lixiang";

    static final String payUrl = "http://limeapi.lx3555.com/pay/submit";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public LiXiang(){
        payTypeMap.put(OutChannel.wechatpay.name(), "wx_qr");//微信扫码支付
        payTypeMap.put(OutChannel.wechath5.name(), "wx_h5");//微信h5支付
        payTypeMap.put(OutChannel.wechatxcx.name(), "wx_pg");//微信小程序支付
        payTypeMap.put(OutChannel.wechatyssm.name(), "wx_sc");//微信原生扫码支付
        payTypeMap.put(OutChannel.wechatysh5.name(), "wx_wp");//微信原生h5支付
        payTypeMap.put(OutChannel.qqpay.name(), "qq_qr");//QQ扫码支付
        payTypeMap.put(OutChannel.qqh5.name(), "qq_h5");//QQh5支付
        payTypeMap.put(OutChannel.alipay.name(), "ali_qr");//支付宝扫码支付
        payTypeMap.put(OutChannel.aliyssm.name(), "ali_pay");//支付宝原生扫码支付
        payTypeMap.put(OutChannel.alih5.name(), "ali_h5");//支付宝h5支付
        payTypeMap.put(OutChannel.aliysh5.name(), "ali_org");//支付宝原生h5支付
        payTypeMap.put(OutChannel.quickpay.name(), "quick_pay");//快捷支付
        payTypeMap.put(OutChannel.jdpaysm.name(), "jd_qr");//京东扫码
        payTypeMap.put(OutChannel.jdh5.name(), "jd_h5");//京东h5

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "利享支付，请求：{}", JSON.toJSONString(reqParams));
        //根据入参加工参数成利享支付需要的参数与签名
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);


        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(payUrl, params));
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "利享支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("tradeType", "pay.submit");//交易类型
        params.put("channel", payType);//支付渠道
        params.put("mchNo", upMerchantNo);//商户号
        params.put("mchOrderNo", orderNo);//商户支付订单
        params.put("amount",String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));//交易金额
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        params.put("timePaid", df.format(new Date()));//订单提交时间
        params.put("bankType", "ICBC");//银行编码
        params.put("notifyUrl",getCallbackUrl(channelNo, merchNo, orderNo));//后台通知回调地址
        String buildParams = SignUtils.buildParams(params, true) + "&paySecret=" + upPublicKey;
        String sign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        params.put("sign", sign);


        params.put("remark",reqParams.getMemo());//备注
        params.put("version", "1.0");//版本号
        params.put("currency", "CNY");//支付银行
        params.put("callbackUrl",reqParams.getReturnUrl());//回调地址
        params.put("goodsDesc", reqParams.getProduct());//商品描述


        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "利享支付异步回调内容：{}", JSON.toJSONString(params));
        String amount =params.get("amount");
        String systemNo = params.get("mchOrderNo");
        String paystatus = params.get("resultCode");
        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "利享支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "OK";
        }
        String upMerchantKey = mcpConfig.getUpKey();
        if (!"0".equals(paystatus)) {
            LogByMDC.error(channelNo, "利享支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "OK";
        }
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "利享支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "利享支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "利享支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "OK";
    }


    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        params.remove("status");
        params.remove("errMsg");
        params.remove("message");
        params.remove("remark");

        String signParams = SignUtils.buildParams(params, true);
        LogByMDC.info(channelNo, "利享支付回调订单:{}，参与验签参数:{}", params.get("mchOrderNo"), signParams);
        String buildParams = signParams + "&paySecret="+upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toUpperCase();
        return newSign.equals(sign);
    }
}
