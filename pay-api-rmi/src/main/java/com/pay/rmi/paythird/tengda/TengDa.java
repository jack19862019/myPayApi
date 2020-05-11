package com.pay.rmi.paythird.tengda;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service(TengDa.channelNo)
public class TengDa extends AbstractPay {
    public static final String channelNo = "tengda";

    static final String payUrl = "http://182.61.173.149:810/pay/api.php";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public TengDa() {
        payTypeMap.put(OutChannel.alipay.name(), "alipay");//支付宝扫码支付
        payTypeMap.put(OutChannel.wechatpay.name(), "weixin");//微信扫码支付
        payTypeMap.put(OutChannel.onlinepay.name(), "wangyin");//网银
        payTypeMap.put(OutChannel.unionsm.name(), "yinliansaoma");//银联
        payTypeMap.put(OutChannel.unionquickpay.name(), "kuaijie");//快捷
        payTypeMap.put(OutChannel.jdpay.name(), "jingdong");//京东
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "腾达支付，请求：{}", JSON.toJSONString(reqParams));
        //根据入参加工参数成通汇宝支付需要的参数与签名
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
        Assert.notNull(payType, "腾达支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("shid", upMerchantNo);//商户号
        params.put("bb", "1.0");
        params.put("zftd", payType);//签名类型
        params.put("ddh", orderNo);//商户订单编号

        BigDecimal money = new BigDecimal(amount);
        DecimalFormat df = new DecimalFormat("#.00");
        params.put("je", df.format(money));
        params.put("ddmc", "666");
        params.put("ddbz", "666");
        if ("wangyin".equals(payType)) {
            params.put("yhdm", reqParams.getBankCode());
        }
        params.put("ybtz", getCallbackUrl(channelNo, merchNo, orderNo));//后台通知回調地址
        params.put("tbtz", reqParams.getReturnUrl());//后台通知回調地址
        String buildParams ="shid="+upMerchantNo+"&bb=1.0&zftd="+payType+"&ddh="+orderNo+"&je="+df.format(money)+"&ddmc=666&ddbz=666&ybtz="+getCallbackUrl(channelNo, merchNo, orderNo)+"&tbtz="+reqParams.getReturnUrl()+"&"+upPublicKey;
        String sign = Objects.requireNonNull(Md5Utils.MD5(buildParams));

        params.put("sign", sign);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "腾达支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "腾达支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "腾达支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String amount = params.get("je");
        String systemNo = params.get("ddh");
        String paystatus = params.get("status");
        if (!"success".equals(paystatus)) {
            LogByMDC.error(channelNo, "腾达支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "腾达支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "腾达支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParamsIgnoreNull(params);
        LogByMDC.info(channelNo, "腾达支付回调订单:{}，参与验签参数:{}", params.get("ddh"), signParams);
        String status = params.get("status");
        String shid = params.get("shid");
        String zftd = params.get("zftd");
        String ddh = params.get("ddh");
        String je = params.get("je");
        String ybtz = params.get("ybtz");
        String tbtz = params.get("tbtz");
        String buildParams ="status="+status+"&shid="+shid+"&bb=1.0&zftd="+zftd+"&ddh="+ddh+"&je="+je+"&ddmc=666&ddbz=666&ybtz="+ybtz+"&tbtz="+tbtz+"&"+upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams));
        return newSign.equals(sign);
    }
}
