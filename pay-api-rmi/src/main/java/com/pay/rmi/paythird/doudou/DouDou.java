package com.pay.rmi.paythird.doudou;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.*;
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
 * 豆豆
 */
@Service(DouDou.channelNo)
public class DouDou extends AbstractPay {
    public static final String channelNo = "doudou";

    static final String payUrl = "http://douzi.yxbaoxmpay.com/api.php/paypoly/pay";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public DouDou() {
        payTypeMap.put(OutChannel.alipay.name(), "ZfbScan");
        payTypeMap.put(OutChannel.alih5.name(), "ZfbH5");
        payTypeMap.put(OutChannel.aliwap.name(), "ZfbBank");

        payTypeMap.put(OutChannel.wechatpay.name(), "WxScan");
        payTypeMap.put(OutChannel.wechath5.name(), "WxH5");
        payTypeMap.put(OutChannel.wechatwap.name(), "WxBank");

        payTypeMap.put(OutChannel.unionquickpay.name(), "UniPay");
        payTypeMap.put(OutChannel.unionsm.name(), "UniScan");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "豆豆支付，请求：{}", JSON.toJSONString(reqParams));
        //根据入参加工参数成豆豆支付需要的参数与签名
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        //发送支付请求
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);

        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String status = resultMap.get("status");
        Assert.isTrue("10000".equals(status), "豆豆上游返回：" + resultMap.get("info"));

        String data = resultMap.get("data");
        JSONObject jsonObject = JSONObject.parseObject(data);
        String code = jsonObject.getString("code");
        String content = jsonObject.getString("content");
        Assert.isTrue("10000".equals(code), "豆豆上游返回：" + content);

        String list = jsonObject.getString("list");
        Map<String, String> urlMap = JSON.parseObject(list, new TypeReference<Map<String, String>>() {
        });
        String payurl = urlMap.get("payurl");

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(payurl);
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "豆豆不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("PayKey", upMerchantNo);
        params.put("orderid", orderNo);
        params.put("PayType", payType);
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("amount", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));

        String buildParams = SignUtils.buildParams(params) + "&PaySecret=" + upPublicKey;
        String sign = Md5Utils.MD5(buildParams);
        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "豆豆支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "豆豆支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upPublicKey = mcpConfig.getUpKey();
        String sign = params.remove("sign");
        String verifyParams = "LhSn="+params.get("LhSn")+"&PayKey="+params.get("PayKey")
                +"&amount="+params.get("amount")+"&orderid="+params.get("orderid")
                +"&paytime="+params.get("paytime")+"&PaySecret=" + upPublicKey;
        String verifySign = Md5Utils.MD5(verifyParams);
        Assert.isTrue(verifySign.equals(sign),"验签失败,参与验签参数："+verifyParams);


        String amount = params.get("amount");
        String systemNo = params.get("LhSn");

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "豆豆支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "豆豆支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "success";
    }
}
