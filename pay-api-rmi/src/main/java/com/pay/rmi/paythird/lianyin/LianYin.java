package com.pay.rmi.paythird.lianyin;

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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service(LianYin.channelNo)
public class LianYin extends AbstractPay {
    public static final String channelNo = "lianyin";

    static final String payUrl = "http://gateway.lianyin88.com/GateWay/Index";

    static final String onlinekjUrl = "http://gateway.lianyin88.com/FastPay/Index";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public LianYin(){
        payTypeMap.put(OutChannel.wechatpay.name(), "1000");//微信扫码支付
        payTypeMap.put(OutChannel.wechath5.name(), "1002");//微信h5支付
        payTypeMap.put(OutChannel.alipay.name(), "1003");//支付宝扫码支付
        payTypeMap.put(OutChannel.alih5.name(), "1004");//支付宝h5
        payTypeMap.put(OutChannel.qqpay.name(), "1005");//qq支付
        payTypeMap.put(OutChannel.qqh5.name(), "1006");//qqh5支付
        payTypeMap.put(OutChannel.jdpay.name(), "1007");//京东扫码
        payTypeMap.put(OutChannel.jdh5.name(), "1008");//京东h5
        payTypeMap.put(OutChannel.unionsm.name(), "1009");//银联扫码

        //payTypeMap.put(OutChannel.onlinekj.name(), "onlinekj");//网银快捷
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "联银支付，请求：{}", JSON.toJSONString(reqParams));

        //根据入参加工参数成联银支付需要的参数与签名
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        if(params.containsKey("banktype")){
            orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(payUrl, params));
        }else{
            orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(onlinekjUrl, params));
        }
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "联银支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("customer", upMerchantNo);//商户号
        if(!payType.equals("onlinekj")) {
            params.put("banktype", payType);//支付类型
        }
        BigDecimal money = new BigDecimal(amount);
        DecimalFormat dfa = new DecimalFormat("#.00");
        params.put("amount", dfa.format(money));//交易金额
        params.put("orderid", orderNo);//商户支付订单
        params.put("asynbackurl",getCallbackUrl(channelNo, merchNo, orderNo));//后台通知回调地址
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        params.put("request_time", df.format(new Date()));//订单提交时间
        String signParams = SignUtils.buildParams(params, false)  + "&key=" + upPublicKey;
        String sign = Objects.requireNonNull(Md5Utils.MD5(signParams));

        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        System.out.println("params:"+params);
        LogByMDC.info(channelNo, "联银支付异步回调内容：{}", JSON.toJSONString(params));
        String amount =params.get("amount");
        String systemNo = params.get("mchOrderNo");
        String paystatus = params.get("result");
        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "联银支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }
        String upMerchantKey = mcpConfig.getUpKey();
        if (!"1".equals(paystatus)) {
            LogByMDC.error(channelNo, "联银支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "success";
        }
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "联银支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "联银支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "联银支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "success";
    }


    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {

        Map<String, String> param=new LinkedHashMap<>();
        param.put("orderid",params.get("orderid"));
        param.put("result",params.get("result"));
        param.put("amount",params.get("amount"));
        param.put("systemorderid",params.get("systemorderid"));
        param.put("completetime",params.get("completetime"));

        String signParams = SignUtils.buildParams(param, false)  + "&key=" + upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(signParams));
        LogByMDC.info(channelNo, "联银支付回调订单:{}，参与验签参数:{}", params.get("orderid"), signParams);
        return newSign.equals(params.get("sign"));
    }
}
