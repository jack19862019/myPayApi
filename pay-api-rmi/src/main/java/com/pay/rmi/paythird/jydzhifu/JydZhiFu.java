package com.pay.rmi.paythird.jydzhifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.DateUtil;
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
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * JYD支付
 */
@Service(JydZhiFu.channelNo)
public class JydZhiFu extends AbstractPay {

    static final String channelNo = "jydzhifu";

    private static final String payUrl = "https://pay.jydzf.com/pay_index.html";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public JydZhiFu() {
        payTypeMap.put(OutChannel.unionpay.name(), "912");//云闪付扫码
        payTypeMap.put(OutChannel.alih5.name(), "904");//支付宝H5
        payTypeMap.put(OutChannel.aliyssm.name(), "903");//支付宝扫码支付
        payTypeMap.put(OutChannel.wechatyssm.name(), "902");//微信扫码支付
        payTypeMap.put(OutChannel.wechath5.name(), "901");//微信H5
    }

    public static String post(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

    private String abc(Map<String, String> params){
        return SignUtils.buildParams(params);
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "JYD支付请求：{}", JSON.toJSONString(reqParams));
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        //签名过期，等待加QQ群调试
        //String result = post(payUrl, SignUtils.buildParams(params));
        String result = restTemplate.postForObject(payUrl, HttpsParams.buildFormEntity(params), String.class);
        LogByMDC.info(channelNo, "JYD支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String resultStr = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(resultStr, new TypeReference<Map<String, String>>() {
        });

        String status = resultMap.get("status");
        Assert.isTrue("success".equals(status), "JYD支付状态响应:" + resultMap.get("msg"));

        String qrCode = resultData.get("payUrl");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrCode);
        return orderApiRespParams;
    }


    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "JYD支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();
        Map<String, String> params = new TreeMap<>();

        params.put("pay_memberid", upMerchantNo);//商户ID
        params.put("random", "59964865");//随机数
        params.put("pay_orderid", orderNo);//订单ID
        params.put("pay_amount", String.valueOf(new BigDecimal(amount)));//金额，精确后小数点后两位
        params.put("pay_applydate", DateUtil.toStr02(null, new Date()));//申请时间，格式：2018-01-02 01:30:40
        params.put("pay_bankcode", payType);//支付代码（支付方式）
        params.put("pay_notifyurl", getCallbackUrl(channelNo, merchNo, orderNo));//异步通知地址，如果不填则不通知

        LogByMDC.info(channelNo, "JYD支付参与加签内容：{}", params);
        String sign = Md5Utils.MD5(getSignStr(params, upPublicKey));
        assert sign != null;
        params.put("pay_md5sign", sign);
        //params.put("pay_extend", "8888");
        return params;
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "JYD支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "JYD支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "JYD支付回调订单：{}，重复回调", order.getOrderNo());
            return "ok";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "JYD支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String trade_no = params.get("orderId");
        String trade_status = params.get("returncode");
        String amount = params.get("amount");

        if (!"00".equals(trade_status)) {
            LogByMDC.error(channelNo, "AA支付回调订单：{}，支付未成功，不再向下通知", trade_no);
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "JYD支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "JYD支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return "ok";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        params.remove("attach");
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "JYD支付回调订单:{}，参与验签参数:{}", params.get("orderId"), signParams);
        String newSign = Md5Utils.MD5(signParams).toUpperCase();
        return newSign.equals(sign);
    }

}
