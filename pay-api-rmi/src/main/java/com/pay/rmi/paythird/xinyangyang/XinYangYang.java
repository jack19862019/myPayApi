package com.pay.rmi.paythird.xinyangyang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.enums.OrderStatus;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 新洋洋
 */
@Service(XinYangYang.channelNo)
public class XinYangYang extends AbstractPay {

    static final String channelNo = "xinyangyang";

    private final Map<String, String> payTypeMap = new HashMap<>();
    private final Map<String, String> payUrlMap = new HashMap<>();

    public XinYangYang() {
        payTypeMap.put(OutChannel.wechath5.name(), "901");
        payTypeMap.put(OutChannel.alih5.name(), "904");
        payTypeMap.put(OutChannel.wechatpay.name(), "929");
        payTypeMap.put(OutChannel.alipay.name(), "930");
        payTypeMap.put(OutChannel.unionpay.name(), "932");
        payTypeMap.put("zk", "936");
        payTypeMap.put(OutChannel.unionsm.name(), "941");
        payTypeMap.put(OutChannel.unionh5.name(), "942");
        payTypeMap.put("yyzfbzk", "943");
        payTypeMap.put("yyzfbgmh5", "944");
        payTypeMap.put("yytbzf", "945");
        payTypeMap.put("yywysgm", "946");
        payTypeMap.put("yyjhzf", "949");

        payUrlMap.put(OutChannel.wechath5.name(), "http://wxh5.sachiko.cn/Pay_Index_inPay.html ");
        payUrlMap.put(OutChannel.alih5.name(), "http://zfbh5.sachiko.cn/Pay_Index_inPay.html");
        payUrlMap.put(OutChannel.wechatpay.name(), "http://yswx.sachiko.cn/Pay_Index_inPay.html");
        payUrlMap.put(OutChannel.alipay.name(), "http://yszfb.sachiko.cn/Pay_Index_inPay.html");
        payUrlMap.put(OutChannel.unionpay.name(), "http://deyl.sachiko.cn/Pay_Index_inPay.html");
        payUrlMap.put("zk", "http://yinxkzk.sachiko.cn/Pay_Index_inPay.html");
        payUrlMap.put(OutChannel.unionsm.name(), "http://ylsm.sachiko.cn/Pay_Index_inPay.html");
        payUrlMap.put(OutChannel.unionh5.name(), "http://ysf.sachiko.cn/Pay_Index_inPay.html");
        payUrlMap.put("yyzfbzk", "http://zfbzk.sachiko.cn/Pay_Index_inPay.html");
        payUrlMap.put("yyzfbgmh5", "http://zfbgmh5.sachiko.cn/Pay_Index_inPay.html");
        payUrlMap.put("yytbzf", "http://tbzfbh5.sachiko.cn/Pay_Index_inPay.html");
        payUrlMap.put("yywysgm", "http://wysgm.sachiko.cn/Pay_Index_inPay.html");
        payUrlMap.put("yyjhzf", "http://jhm.sachiko.cn/Pay_Index_inPay.html");

    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "新洋洋支付请求：{}", JSON.toJSONString(reqParams));

        String payUrl = payUrlMap.get(reqParams.getOutChannel());

        String payload = mcpConfig.getUpKey();
        LogByMDC.info(channelNo, "新洋洋支付,RSA负载参数：{}", JSON.toJSONString(payload));
        Map<String, String> payloadMap = JSON.parseObject(payload, new TypeReference<Map<String, String>>() {
        });
        String rsaPrivateKey = StringUtils.chomp(payloadMap.get("rsaPrivateKey").trim());
        String rsaPublicKey = StringUtils.chomp(payloadMap.get("rsaPublicKey").trim());

        Map<String, String> params = getParamsMap(mcpConfig, reqParams, rsaPrivateKey);
        params.put("pay_productname", reqParams.getProduct());
        params.put("pay_attach", reqParams.getMemo());
        String str = JSON.toJSONString(params);
        LogByMDC.info(channelNo, "新洋洋支付 订单：{}，RSA二次加签后返回：{}", reqParams.getOrderNo(), str);
        //加密
        String req = RSAEncrypt.encrypt(RSAEncrypt.loadPublicKeyByStr(rsaPublicKey), str.getBytes());
        Map map = new HashMap();
        map.put("req", req);
        map.put("sign", params.get("sign"));
        map.put("merno", mcpConfig.getUpMerchantNo());

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(payUrl, params));

        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams, String rsaPrivateKey) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = "g9uhqqdb751di00pheqdu9x4zow9os2n";
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("pay_memberid", upMerchantNo);
        params.put("pay_orderid", orderNo);
        params.put("pay_applydate", System.currentTimeMillis() + "");
        params.put("pay_bankcode", payType);
        params.put("pay_notifyurl", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("pay_callbackurl", reqParams.getReturnUrl());
        params.put("pay_amount", amount);
        //params.put("pay_productname", reqParams.getProduct());
        //params.put("pay_attach", reqParams.getMemo());
        LogByMDC.info(channelNo, "新洋洋支付,参数组装：{}", params);
        String md5Value = Md5.md5(getSign(params, upPublicKey));
        //RSA私钥再次加签

        String sign = RSASignature.sign(md5Value, rsaPrivateKey);
        params.put("pay_md5sign", sign);
        return params;
    }

    private String getSign(Map<String, String> params, String key) {
        return SignUtils.buildParams(params) + "&key=" + key;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "新洋洋支付回调内容1：{}", params);
        String str = JSON.toJSONString(params);
        str = str.replaceAll(" ", "+");
        params = JSON.parseObject(str, new TypeReference<Map<String, String>>() {
        });
        LogByMDC.info(channelNo, "新洋洋支付回调内容2：{}", params);
        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "新洋洋支付回调订单：{}，重复回调", order.getOrderNo());
            return "OK";
        }
        String md5Key = "g9uhqqdb751di00pheqdu9x4zow9os2n";
        String payload = mcpConfig.getUpKey();
        LogByMDC.info(channelNo, "新洋洋支付回调,RSA负载参数：{}", JSON.toJSONString(payload));
        Map<String, String> payloadMap = JSON.parseObject(payload, new TypeReference<Map<String, String>>() {
        });
        String rsaPrivateKey = payloadMap.get("rsaPrivateKey");
        String rsaPublicKey = payloadMap.get("rsaPublicKey");

        String req = params.get("req"); //加密
        String sign = params.get("sign"); //签名
        //利用RSA私钥(rsa_private_key.pem)对req进行解密得到json字符串
        String ret_d = RSAEncrypt.decrypt(rsaPrivateKey, req);
        LogByMDC.info(channelNo, "新洋洋支付回调,RSA解密后参数：{}", ret_d);
        JSONObject obj = JSONObject.parseObject(ret_d);
        String memberid = obj.getString("memberid");
        String orderid = obj.getString("orderid");
        String amount = obj.getString("amount");
        String datetime = obj.getString("datetime");
        String returncode = obj.getString("returncode");
        String transaction_id = obj.getString("transaction_id");
        String SignTemp = "amount=" + amount + "&datetime=" + datetime + "&memberid=" + memberid + "&orderid=" + orderid + "&returncode=" + returncode + "&transaction_id=" + transaction_id + "&key=" + md5Key;
        String md5sign = Md5.md5(SignTemp);//MD5加密
        //利用RSA公钥(rsa_public_key.pem)进行验签
        boolean res_veitify = RSASignature.doCheck(md5sign, sign, rsaPublicKey);
        if (!res_veitify) {
            throw new RException("新洋洋支付验签失败");
        }
        if (!"00".equals(returncode)) {
            LogByMDC.error(channelNo, "新洋洋支付回调订单：{}，支付未成功，不再向下通知", order.getOrderNo());
            return "OK";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(transaction_id);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "新洋洋支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "新洋洋支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("通知下游报错:" + e.getMessage());
        }
        return "OK";
    }


}
