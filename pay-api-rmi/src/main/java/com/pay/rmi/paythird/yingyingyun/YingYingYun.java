package com.pay.rmi.paythird.yingyingyun;

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
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 赢赢云
 */
@Service(YingYingYun.channelNo)
public class YingYingYun extends AbstractPay {

    public static final String channelNo = "yingyingyun";

    private final String payUrl = "http://gateway.winner-winner.cn/order/pay";

    private final String queryUrl = "http://gateway.winner-winner.cn/payapi/query/orderstatus";

    private final Map<String, String> payTypeMap = new HashMap<>();

    private DecimalFormat df = new DecimalFormat("#.00");

    public YingYingYun() {
        payTypeMap.put(OutChannel.alipay.name(), "ALIPAY");
        payTypeMap.put(OutChannel.alih5.name(), "ALIPAYH5");
        payTypeMap.put(OutChannel.wechatpay.name(), "WEIXIN");
        payTypeMap.put(OutChannel.wechath5.name(), "WEIXINH5");
        payTypeMap.put(OutChannel.qqpay.name(), "QQ");
        payTypeMap.put(OutChannel.qqh5.name(), "QQH5");
        payTypeMap.put(OutChannel.jdpay.name(), "JD");
        payTypeMap.put(OutChannel.jdh5.name(), "JDH5");
        payTypeMap.put(OutChannel.unionpay.name(), "YINLIAN");
        payTypeMap.put(OutChannel.quickpay.name(), "KUAIJIE");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "赢赢云支付请求：{}", JSON.toJSONString(reqParams));

        String payType = payTypeMap.get(reqParams.getOutChannel());
        if (StringUtils.isBlank(payType)) {
            Assert.notNull(payType, "赢赢云不支持的支付方式:" + reqParams.getOutChannel());
        }
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();


        Map<String, String> params = new HashMap<>();
        //Account ID，由赢赢云分配
        params.put("accountid", upMerchantNo);
        //通道类型
        params.put("type", payType);
        //单位元(人民币)，2 位小数，最小支付 金额为 0.02
        params.put("amount", df.format(new BigDecimal(reqParams.getAmount())));
        //订单号
        params.put("orderid",  reqParams.getOrderNo());
        //异步通知地址
        params.put("notifyurl", getCallbackUrl(channelNo, reqParams.getMerchNo(), reqParams.getOrderNo()));

        String sign = Md5Utils.sign(params, "&authtoken=" + upPublicKey);

        //支付用户 IP
        params.put("clientip",reqParams.getReqIp() );

        params.put("sign", sign.toUpperCase());

        LogByMDC.info(channelNo, "订单：{}，request：{}",  reqParams.getOrderNo(), JSON.toJSONString(params));

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(payUrl, params));
        return orderApiRespParams;
    }


    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "赢赢云回调内容：{}", params);

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("code", "success");
        resultMap.put("msg", "");
        String result = JSON.toJSONString(resultMap);

        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "赢赢云订单：{}，重复回调", order.getOrderNo());
            return result;
        }

        String orderNo = order.getOrderNo();

        String upMerchantKey =  mcpConfig.getUpKey();

        String upSign = params.get("sign");
        params.remove("sign");
        params.remove("desc");
        params.remove("completetime");

        String platformorderid = params.get("platformorderid");
        params.remove("platformorderid");

        String sign = Md5Utils.sign(params, "&authtoken=" + upMerchantKey).toUpperCase();
        if (!sign.equals(upSign)) {
            LogByMDC.error(channelNo, "赢赢云订单：{}，验证回调签名错误", orderNo);
            return result;
        }

        String amount = params.get("amount");

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(platformorderid);
        orderService.update(order);

        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "赢赢云订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "赢赢云订单：{}，下发通知失败", order.getOrderNo());
        }

        return result;
    }
}
