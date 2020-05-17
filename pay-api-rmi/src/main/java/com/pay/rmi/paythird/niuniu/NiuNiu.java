package com.pay.rmi.paythird.niuniu;

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
import com.pay.rmi.common.utils.HttpsParams;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
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

@Service(NiuNiu.channelNo)
public class NiuNiu extends AbstractPay {
    public static final String channelNo = "niuniu";

    static final String payUrl = "https://niuapi.ikoko.cc/v1/ordercreate";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public NiuNiu() {
        payTypeMap.put(OutChannel.alipay.name(), "aliqr");//支付宝扫码支付
        payTypeMap.put(OutChannel.wechatpay.name(), "wxqr");//微信扫码支付
        payTypeMap.put(OutChannel.onlinepay.name(), "banktrans");//银行卡转账
        payTypeMap.put(OutChannel.qqpay.name(), "qqqr");//QQ扫码支付
        payTypeMap.put(OutChannel.unionwap.name(), "yunqr");//云闪付（银联扫码）
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "牛牛支付，请求：{}", JSON.toJSONString(reqParams));

        //根据入参加工参数成通汇宝支付需要的参数与签名
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "牛牛支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);

        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String code = resultMap.get("code");
        Assert.isTrue("0".equals(code), "牛牛支付状态响应:" + resultMap.get("msg"));

        String http_url = resultMap.get("http_url");
        Assert.isTrue(!StringUtils.isEmpty(http_url), "牛牛支付响应未返回支付路徑");

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(http_url);
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "牛牛支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("login_id", upMerchantNo);//商户号
        params.put("pay_type", payType);//签名类型
        params.put("create_ip", reqParams.getReqIp());//客户端IP
        params.put("sign_type", "MD5");//签名类型
        params.put("order_type", "1");//订单类型
        params.put("order_sn", orderNo);//商户订单编号
        params.put("nonce", System.currentTimeMillis() + "");//随机数
        params.put("send_currency", "cny");//货币类型
        params.put("recv_currency", "cny");//货币类型
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));//后台通知回調地址
        BigDecimal money = new BigDecimal(amount);
        DecimalFormat df = new DecimalFormat("#.00");
        params.put("amount", df.format(money));
        String time = System.currentTimeMillis() + "";
        params.put("create_time", time.substring(0, 10));//请求时间
        String buildParams = SignUtils.buildParams(params) + "&api_secret=" + upPublicKey;

        String sign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toLowerCase();

        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "牛牛支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "牛牛支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "牛牛支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String amount = params.get("amount");
        String systemNo = params.get("order_sn");
        String paystatus = params.get("pay_state");
        if (!"1".equals(paystatus)) {
            LogByMDC.error(channelNo, "牛牛支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "牛牛支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "牛牛支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParamsIgnoreNull(params);
        LogByMDC.info(channelNo, "牛牛支付回调订单:{}，参与验签参数:{}", params.get("order_sn"), signParams);
        String buildParams = signParams + "&api_secret=" + upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams)).toLowerCase();
        return newSign.equals(sign);
    }
}
