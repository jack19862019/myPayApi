package com.pay.rmi.paythird.jinruixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.pay.rmi.common.utils.HttpsParams;
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

/**
 * 金瑞鑫
 */
@Service(JinRuiXin.channelNo)
public class JinRuiXin extends AbstractPay {

    static final String channelNo = "jinruixin";

    private static final String payUrl = "http://pay.newbiaobai.com/Pay_Index.html";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public JinRuiXin() {
        payTypeMap.put(OutChannel.wechatgz.name(), "901");//微信公众号
        payTypeMap.put(OutChannel.wechatpay.name(), "902");//微信扫码支付
        payTypeMap.put(OutChannel.alipay.name(), "903");//支付宝扫码支付
        payTypeMap.put(OutChannel.alih5.name(), "904");//支付宝手机
        payTypeMap.put(OutChannel.qqh5.name(), "905");//QQ手机支付
        payTypeMap.put(OutChannel.onlinepay.name(), "907");//网银支付
        payTypeMap.put(OutChannel.qqpay.name(), "908");//QQ扫码支付
        payTypeMap.put(OutChannel.baidupay.name(), "909");//百度钱包
        payTypeMap.put(OutChannel.jdpay.name(), "910");//京东支付
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig , OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "金瑞鑫支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());

        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);

        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "金瑞鑫返回参数：{}",result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String data = resultMap.get("data");
        JSONObject jsonObject = JSONObject.parseObject(data);
        String payurl = jsonObject.getString("payUrl");
        orderApiRespParams.setCode_url(payurl);
        return orderApiRespParams;

    }



    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "金瑞鑫不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();
        Map<String, String> params = new TreeMap<>();
        params.put("pay_memberid", upMerchantNo);
        params.put("pay_orderid", orderNo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        params.put("pay_applydate", sdf.format(new Date()));
        params.put("pay_bankcode", payType);
        params.put("pay_notifyurl",  getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("pay_callbackurl", reqParams.getReturnUrl());
        params.put("pay_amount", amount);
        String signParams =  SignUtils.buildParams(params, true)   + "&key=" + upPublicKey;
        String sign = Objects.requireNonNull(Md5Utils.MD5(signParams)).toUpperCase();
        params.put("pay_username", "JRX200167247");
        params.put("pay_md5sign", sign);
        params.put("pay_productname", "商品名称");

        params.put("pay_reserved1", "1");
        params.put("pay_reserved2", "2");
        params.put("pay_reserved3", "3");
        params.put("pay_productnum", "4");
        params.put("pay_productdesc", "5");
        params.put("pay_producturl", "6");

        return params;
    }



    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "金瑞鑫支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "金瑞鑫支付回调订单：{}，重复回调", order.getOrderNo());
            return "OK";
        }
        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "金瑞鑫支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }
        String trade_no = params.get("orderid");
        String returnCode = params.get("returncode");
        String amount = params.get("amount");

        if (!"00".equals(returnCode)) {
            LogByMDC.error(channelNo, "金瑞鑫支付回调订单：{}，支付未成功{}，不再向下通知", trade_no);
            return "OK";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "金瑞鑫支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "金瑞鑫支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下金瑞鑫支付发通知报错:" + e.getMessage());
        }
        return "OK";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        Map<String, String> paramTree = new TreeMap<>();
        paramTree.put("amount", params.get("amount"));
        paramTree.put("datetime", params.get("datetime"));
        paramTree.put("memberid", params.get("memberid"));
        paramTree.put("orderid", params.get("orderid"));
        paramTree.put("transaction_id", params.get("transaction_id"));
        paramTree.put("returncode", params.get("returncode"));
        String signParams =  SignUtils.buildParams(paramTree, true)   + "&key=" + upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(signParams)).toUpperCase();
        return newSign.equals(params.get("sign"));
    }

}
