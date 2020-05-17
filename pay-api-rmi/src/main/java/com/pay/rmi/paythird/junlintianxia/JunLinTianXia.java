package com.pay.rmi.paythird.junlintianxia;

import com.alibaba.fastjson.JSON;
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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service(JunLinTianXia.channelNo)
public class JunLinTianXia extends AbstractPay {
    public static final String channelNo = "junlintianxia";

    static final String payUrl = "https://jlpay-api.com/gateway";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public JunLinTianXia() {
        payTypeMap.put(OutChannel.alipay.name(), "alipay_qr");//支付宝扫码支付
        payTypeMap.put(OutChannel.wechatpay.name(), "wechat_qr");//微信扫码支付
    }
    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "君临天下支付，请求：{}", JSON.toJSONString(reqParams));

        //根据入参加工参数成通汇宝支付需要的参数与签名
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        //String result=null;
        LogByMDC.info(channelNo, "君临天下支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);

        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String code = resultMap.get("success");
        Assert.isTrue("0".equals(code), "君临天下支付状态响应:" + resultMap.get("msg"));

        String payUrl = resultMap.get("pay_url");
        Assert.isTrue(!StringUtils.isEmpty(payUrl), "君临天下支付响应未返回支付二维码");


        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(payUrl);
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "君临天下支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("mch_id", upMerchantNo);//商户号
        params.put("pay_type", payType);
        params.put("out_trade_no", orderNo);//商户订单编号
        params.put("user_code", "123456");
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));//后台通知回調地址
        BigDecimal money = new BigDecimal(amount);
        DecimalFormat df = new DecimalFormat("#.00");
        params.put("order_amount", df.format(money));

        String buildParams = SignUtils.buildParams(params, true)  + upPublicKey;
        String sign = Md5Utils.MD5(buildParams);
        assert sign != null;
        params.put("sign", sign);
        return params;
    }


    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "君临天下支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "君临天下支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "君临天下支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String amount = params.get("pay_amount");
        String systemNo = params.get("out_order_no");
        String paystatus = params.get("order_state");
        if (!"1".equals(paystatus)) {
            LogByMDC.error(channelNo, "君临天下支付订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "君临天下支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "君临天下支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params, true) ;
        LogByMDC.info(channelNo, "君临天下支付回调订单:{}，参与验签参数:{}", params.get("out_order_no"), signParams);
        String buildParams = signParams + upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams));
        return newSign.equals(sign);
    }
}
