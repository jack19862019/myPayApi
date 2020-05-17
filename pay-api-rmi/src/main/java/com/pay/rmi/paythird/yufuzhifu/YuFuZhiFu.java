package com.pay.rmi.paythird.yufuzhifu;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service(YuFuZhiFu.channelNo)
public class YuFuZhiFu extends AbstractPay {
    public static final String channelNo = "yufuzhifu";

    static final String payUrl = "https://pay.yfpay999.com/Pay";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public YuFuZhiFu(){
        payTypeMap.put(OutChannel.wechatpay.name(), "4");//微信扫码支付
        payTypeMap.put(OutChannel.alipay.name(), "27");//支付宝扫码支付
        payTypeMap.put(OutChannel.alih5.name(), "28");//支付宝h5支付
        payTypeMap.put(OutChannel.quickpay.name(), "29");//快捷支付
        payTypeMap.put(OutChannel.unionpay.name(), "30");//银联网关
        payTypeMap.put(OutChannel.unionsm.name(), "31");//银联扫码
        payTypeMap.put(OutChannel.wechath5.name(), "32");//微信h5支付
        payTypeMap.put(OutChannel.unionwap.name(), "33");//银联扫码

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "御付支付，请求：{}", JSON.toJSONString(reqParams));

        //根据入参加工参数成花花支付需要的参数与签名
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
        Assert.notNull(payType, "御付支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("merchant_id", upMerchantNo);//商户号
        params.put("payment_way", payType);//支付方式
        params.put("order_amount", new DecimalFormat("#.00").format(new BigDecimal(amount)));//订单金额
        params.put("source_order_id", orderNo);//商家订单号
        params.put("goods_name", reqParams.getProduct());//商品名称
        params.put("bank_code", "ICBC");//网银直连银行代码
        params.put("client_ip", reqParams.getReqIp());//客户端IP
        params.put("notify_url",getCallbackUrl(channelNo, merchNo, orderNo));//异步通知url
        params.put("return_url",reqParams.getReturnUrl());//页面跳转同步通知地址

        String buildParams = SignUtils.buildParams(params) + "&token=" + upPublicKey;
        String sign = Objects.requireNonNull(Md5Utils.MD5(buildParams));
        params.put("sign", sign);
        //params.put("bank_no",reqParams.getBankAccountNo());//银行卡号
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "御付支付异步回调内容：{}", JSON.toJSONString(params));
        String amount =params.get("order_amount");
        String systemNo = params.get("source_order_id");
        String paystatus = params.get("status");
        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "御付支付异步回调订单：{}，重复回调", order.getOrderNo());
            System.out.println("************1*************");
            return "ok";
        }
        String upMerchantKey = mcpConfig.getUpKey();
        if (!"1".equals(paystatus)) {
            LogByMDC.error(channelNo, "御付支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            System.out.println("************2*************");
            return "ok";
        }
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "御付支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "御付支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "御付支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        System.out.println("**********3************");
        return "ok";
    }


    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");

        String signParams = SignUtils.buildParams(params);
        LogByMDC.info(channelNo, "御付支付回调订单:{}，参与验签参数:{}", params.get("mchOrderNo"), signParams);
        String buildParams = SignUtils.buildParams(params) + "&token=" + upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams));
        return newSign.equals(sign);
    }
}
