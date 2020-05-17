package com.pay.rmi.paythird.kuaiyin;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 快银
 */
@Service(KuaiYin.channelNo)
public class KuaiYin extends AbstractPay {

    static final String channelNo = "kuaiyin";

    private static final String payUrl = "http://gateway.fastwinpro.com/pay/index.aspx";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public KuaiYin() {
        payTypeMap.put(OutChannel.alipay.name(), "1002");
        payTypeMap.put(OutChannel.alih5.name(), "1006");

        payTypeMap.put(OutChannel.wechatpay.name(), "1001");
        payTypeMap.put(OutChannel.wechath5.name(), "1005");

        payTypeMap.put(OutChannel.qqpay.name(), "1007");
        payTypeMap.put(OutChannel.qqh5.name(), "1008");

        payTypeMap.put(OutChannel.unionpay.name(), "1013");
        payTypeMap.put(OutChannel.unionh5.name(), "1014");

        payTypeMap.put(OutChannel.jdpay.name(), "1009");
        payTypeMap.put(OutChannel.jdh5.name(), "1010");

        payTypeMap.put("szfbsm", "1016");
        payTypeMap.put("szfbh5", "1017");

        payTypeMap.put("swxsm", "1018");
        payTypeMap.put("swxh5", "1019");

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "快银支付请求：{}", JSON.toJSONString(reqParams));

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
        Assert.notNull(payType, "不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("merchant", upMerchantNo);
        params.put("version", "1.0");
        params.put("paytype", payType);
        params.put("orderid", orderNo);
        params.put("amount", new BigDecimal(amount) + "");
        params.put("ordertime", System.currentTimeMillis() + "");
        params.put("returnurl", reqParams.getReturnUrl());
        params.put("notifyurl", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("signtype", "1");
        params.put("ip", reqParams.getReqIp());
        String sign = Md5Utils.MD5(getSign(params, upPublicKey));
        params.put("sign", sign);
        return params;
    }

    private String getSign(Map<String, String> params, String upPublicKey) {
        StringBuffer sb = new StringBuffer();
        sb.append("amount=" + params.get("amount"))
                .append("merchant=" + params.get("merchant"))
                .append("notifyurl=" + params.get("notifyurl"))
                .append("orderid=" + params.get("orderid"))
                .append("ordertime=" + params.get("ordertime"))
                .append("paytype=" + params.get("paytype"))
                .append("returnurl=" + params.get("returnurl"))
                .append("signtype=" + params.get("signtype"))
                .append("version=" + params.get("version"));
        return sb.toString() + upPublicKey;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "快银支付回调内容：{}", params);
        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "快银支付回调订单：{}，重复回调", order.getOrderNo());
            return "10000";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "快银支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "FAIL";
        }
        String trade_no = params.get("orderid");
        String trade_status = params.get("code");
        String amount = params.get("oamount");
        String msg = params.get("msg");

        if (!"10000".equals(trade_status)) {
            LogByMDC.error(channelNo, "快银支付回调订单：{}，支付未成功{}，不再向下通知", trade_no, msg);
            return "10000";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "快银支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "快银支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return "10000";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        Map<String, String> treeMap = new TreeMap<>(params);
        String sign = treeMap.remove("sign");
        treeMap.remove("attach");
        treeMap.remove("msg");
        String signParams = SignUtils.buildParams(treeMap);
        signParams = signParams.replace("&", "");
        LogByMDC.info(channelNo, "快银支付回调订单:{}，参与验签参数:{}", treeMap.get("orderid"), signParams);
        String buildParams = signParams + upMerchantKey;
        String newSign = Md5Utils.MD5(buildParams);
        return newSign.equals(sign);
    }

}
