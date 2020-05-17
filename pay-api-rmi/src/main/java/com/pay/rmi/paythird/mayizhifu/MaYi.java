package com.pay.rmi.paythird.mayizhifu;

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

@Service(MaYi.channelNo)
public class MaYi extends AbstractPay {
    public static final String channelNo = "mayi";

    static final String payUrl = "http://api.juhe123.net/manager/abutting/recharge";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public MaYi() {
        payTypeMap.put(OutChannel.alipay.name(), "1");//支付宝扫码支付
        payTypeMap.put(OutChannel.alipdd.name(), "3");//pdd扫码支付
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "蚂蚁支付，请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        //发送支付请求
        String result = restTemplate.postForObject(payUrl,params, String.class);
        LogByMDC.info(channelNo, "蚂蚁支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("code");
        Assert.isTrue("200".equals(code), "蚂蚁支付状态响应:" + resultMap.get("data"));
        String data = resultMap.get("data");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(data);
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "蚂蚁支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("businessOrderSn", orderNo);//商户订单编号
        params.put("appKey",upMerchantNo);//appid
        params.put("amount", amount);
        params.put("payType", payType);//签名类型
        params.put("callBackUrl", getCallbackUrl(channelNo, merchNo, orderNo));//后台通知回調地址
        params.put("remarks", "test");
        String buildParams =params.get("appKey")+upPublicKey+params.get("businessOrderSn");
        String sign =Md5Utils.MD5(buildParams);

        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "蚂蚁支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "蚂蚁支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "蚂蚁支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String amount = params.get("amount");
        String systemNo = params.get("businessOrderSn");
        String paystatus = params.get("status");
        if (!"1".equals(paystatus)) {
            LogByMDC.error(channelNo, "蚂蚁支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "蚂蚁支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "蚂蚁支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParamsIgnoreNull(params);
        LogByMDC.info(channelNo, "蚂蚁支付回调订单:{}，参与验签参数:{}", params.get("ddh"), signParams);
        String buildParams ="2a8b3af2c7124c419bd5bc050cf9b3d7"+params.get("businessOrderSn")+params.get( "status");
        String newSign =Md5Utils.MD5(buildParams).toUpperCase();
        return newSign.equals(sign);
    }
}
