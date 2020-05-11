package com.pay.rmi.paythird.xunke;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.enums.OrderStatus;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.*;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.pay.rmi.paythird.xunke.util.EkaPayEncrypt;
import com.pay.rmi.paythird.xunke.util.HttpUtil;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.*;

@Service(XunKe.channelNo)
public class XunKe extends AbstractPay {
    public static final String channelNo = "xunke";

    static final String payUrl = "http://pay.xkzhifu.com:12223/";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public XunKe() {
        payTypeMap.put(OutChannel.alipay.name(), "992");//支付宝扫码支付
        payTypeMap.put(OutChannel.alih5.name(), "1006");//支付宝h5
        payTypeMap.put(OutChannel.unionpay.name(), "1005");//云闪付
        payTypeMap.put(OutChannel.onlinepaysm.name(), "970");//网银快捷H5
        payTypeMap.put(OutChannel.wechatpay.name(), "1007");//微信扫码支付
        payTypeMap.put(OutChannel.wechath5.name(), "1004");//微信h5
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "讯科支付，请求：{}", JSON.toJSONString(reqParams));

        String parter = mcpConfig.getUpMerchantNo();
        String md5key = mcpConfig.getUpKey();
        String type = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(type, "讯科支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderid = reqParams.getOrderNo();
        String callbackurl = getCallbackUrl(channelNo, merchNo, orderid);//支付结果异步地址

        String value = String.valueOf(new BigDecimal(reqParams.getAmount()).intValue());//支付面值
        String sign = EkaPayEncrypt.EkaPayBankMd5Sign(type,parter,value,orderid,callbackurl,md5key);//签名

        String params = "parter="+parter+"&type="+type+"&value="+value+"&orderid="+orderid+"&callbackurl="+callbackurl
                +"&sign="+sign+"&onlyqrcode=1";
        String url = "";
        if(type.equals("992") || type.equals("1006")){
            url=payUrl + "alipayBank.aspx";
        }else if(type.equals("1007") || type.equals("1004")){
            url=payUrl + "wxpayBank.aspx";
        }else if(type.equals("1005")){
            url=payUrl + "unionpayBank.aspx";
        }else{
            url=payUrl + "ChargeBank.aspx";
        }
        String result = HttpUtil.sendGetHttp(url, params);

        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("code");
        Assert.isTrue("1".equals(code), "虾米支付状态响应:" + resultMap.get("msg"));

        String qrcode = resultMap.get("qrcode");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrcode);
        return orderApiRespParams;
    }


    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "讯科支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "讯科支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "opstate=0";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "讯科支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "opstate=1";
        }

        String amount = params.get("ovalue");
        String systemNo = params.get("orderid");
        String paystatus = params.get("opstate");
        if (!"0".equals(paystatus)) {
            LogByMDC.error(channelNo, "讯科支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "opstate=0";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "虾米支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "虾米支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "opstate=0";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params);
        LogByMDC.info(channelNo, "讯科支付回调订单:{}，参与验签参数:{}", params.get("orderid"), signParams);

        String orderid = params.get("orderid");
        String opstate = params.get("opstate");
        String ovalue = params.get("ovalue");
        String newSign = EkaPayEncrypt.EkaPayCardBackMd5Sign(orderid,opstate,ovalue,upMerchantKey);//签名
        return newSign.equals(sign);
    }
}

