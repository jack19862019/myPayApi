package com.pay.rmi.paythird.bangFu;

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
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service(BangFu.channelNo)
public class BangFu extends AbstractPay {
    public static final String channelNo = "bangfu";

    static final String aliUrl = "http://api.bonfu88.net/AliQRCodePayment.php";

    static final String unionUrl = "http://api.bonfu88.net/UnionYSFQRCodePayment.php";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "邦富支付，请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        switch (reqParams.getOutChannel()) {
            case "alipay":
                orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(aliUrl, params));
                break;
            case "unionpay":
                orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(unionUrl, params));
                break;
            default:
                LogByMDC.error(channelNo, "邦富支付不支持的支付方式", params);
                break;
        }
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();

        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("MerID", upMerchantNo);//商户号
        params.put("MerTradeID", orderNo);//商户支付订单
        params.put("MerProductID", "123456");//店家商品代号
        params.put("MerUserID", "123456");//消费者id
        BigDecimal money = new BigDecimal(amount);
        DecimalFormat df = new DecimalFormat("#.00");
        params.put("Amount", df.format(money));
        String buildParams = upMerchantNo+ orderNo+params.get("MerProductID")+params.get("MerUserID")+params.get("Amount")+ upPublicKey;

        params.put("TradeDesc", "交易描述");//交易描述
        params.put("ItemName", "商品名称");//商品名称
        params.put("NotifyUrl",getCallbackUrl(channelNo, merchNo, orderNo));//后台通知回调地址
        params.put("ReturnCodeURL","0");//由邦富生成支付页面

        String sign = Objects.requireNonNull(Md5Utils.MD5(buildParams));
        params.put("Sign", sign);
        return params;
    }

    @Override
        public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "邦富支付异步回调内容：{}", JSON.toJSONString(params));
        String amount =params.get("RealPayAmount");
        String systemNo = params.get("MerTradeID");
        String paystatus = params.get("RtnCode");
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "邦富支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }
        String upMerchantKey = mcpConfig.getUpKey();
        if (!"1".equals(paystatus)) {
            LogByMDC.error(channelNo, "邦富支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "success";
        }
        boolean signVerify = verifySignParams(params, upMerchantKey,mcpConfig.getUpMerchantNo());
        if (!signVerify) {
            LogByMDC.error(channelNo, "邦富支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "邦富支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "邦富支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "success";
    }


    private boolean verifySignParams(Map<String, String> params, String upMerchantKey,String merID) {
        String sign = params.remove("Sign");

       String signParams = "MerID="+merID+"&RtnCode="+params.get("RtnCode")
               +"&MerTradeID="+params.get("MerTradeID")  +"&MerUserID="+params.get("MerUserID")  +"&Amount="+params.get("Amount")
               +"&SignKey="+ upMerchantKey;
        LogByMDC.info(channelNo, "邦富支付回调订单:{}，参与验签参数:{}", params.get("MerTradeID"), signParams);
        String newSign = Md5Utils.MD5(signParams);
        return newSign.equals(sign);
    }
}
