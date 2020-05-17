package com.pay.rmi.paythird.huifeng;

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
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.HttpsParams;
import com.pay.rmi.common.utils.LogByMDC;
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
 * 汇丰
 */
@Service(HuiFeng.channelNo)
public class HuiFeng extends AbstractPay {

    static final String channelNo = "huifeng";

    private static final String payUrl = "https://www.hfpay1.cc/pay";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public HuiFeng() {
        payTypeMap.put(OutChannel.alipay.name(), "alipay");
        payTypeMap.put(OutChannel.alih5.name(), "alipayh5");
        payTypeMap.put(OutChannel.wjzx.name(), "bankpay");//网关支付
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "汇丰支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "汇丰支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String status = resultMap.get("status");
        Assert.isTrue("1".equals(status), "汇丰上游支付状态响应:" + resultMap.get("error"));
        String qrCode = resultMap.get("payurl");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrCode);
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
        BigDecimal bigDecimal = new BigDecimal(amount);
        int iAmount = bigDecimal.intValue();

        Map<String, String> params = new TreeMap<>();
        params.put("fxid", upMerchantNo);
        params.put("fxddh", orderNo);
        params.put("fxdesc", reqParams.getProduct());
        params.put("fxpay", payType);
        params.put("fxfee", iAmount + "");
        params.put("fxbackurl", reqParams.getReturnUrl());
        params.put("fxnotifyurl", getCallbackUrl(channelNo, merchNo, orderNo));
        //params.put("fxnotifystyle", "2");
        params.put("fxip", reqParams.getReqIp());

        String sign = Md5Utils.MD5(getSign(params, upPublicKey));
        assert sign != null;
        params.put("fxsign", sign.toLowerCase());
        return params;
    }

    private String getSign(Map<String, String> params, String upPublicKey) {
        StringBuffer sb = new StringBuffer();
        sb.append(params.get("fxid"))
                .append(params.get("fxddh"))
                .append(params.get("fxfee"))
                .append(params.get("fxnotifyurl"));
        return sb.toString() + upPublicKey;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "汇丰支付回调内容：{}", params);
        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "汇丰支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        String fxddh = params.get("fxddh");
        String fxstatus = params.get("fxstatus");
        String fxid = params.get("fxid");
        String fxfee = params.get("fxfee");
        String fxsign = params.get("fxsign");
        boolean signVerify = verifySignParams(fxddh, fxstatus, fxid, fxfee, fxsign, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "汇丰支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "FAIL";
        }
        String trade_no = params.get("fxddh");
        String trade_status = params.get("fxstatus");
        String amount = params.get("fxrealfee");

        if (!"1".equals(trade_status)) {
            LogByMDC.error(channelNo, "汇丰支付回调订单：{}，支付未成功，不再向下通知", order.getOrderNo());
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "汇丰支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "汇丰支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return "success";
    }

    private boolean verifySignParams(String fxddh, String fxstatus, String fxid, String fxfee, String fxsign, String upMerchantKey) {
        String verifySignParams = fxstatus + fxid + fxddh + fxfee + upMerchantKey;
        LogByMDC.info(channelNo, "汇丰支付回调验签订单:{}，参与验签参数:{}", fxddh, verifySignParams);
        String newSign = Md5Utils.MD5(verifySignParams);
        return newSign.equals(fxsign);
    }
}
