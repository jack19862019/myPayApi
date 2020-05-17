package com.pay.rmi.paythird.quantong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.enums.OrderStatus;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


@Service(QuanTong.channelNo)
public class QuanTong extends AbstractPay {

    public static final String channelNo = "quantong";

    static final String payUrl = "https://www.allpasspay.com/hspay/api_node";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    //alipay  支付宝扫码支付
    //alipaywap 支付宝WAP支付
    //alipaycode 支付宝付款码支付
    //weixin  微信扫码支付
    //wxwap  微信wap支付
    //weixincode 微信付款码支付
    //bdpay,银联扫码
    //yinlian  快捷支付
    public QuanTong() {
        payTypeMap.put(OutChannel.wechatpay.name(), "weixin");
        payTypeMap.put(OutChannel.alipay.name(), "alipay");
        payTypeMap.put(OutChannel.unionquickpay.name(), "yinlian");
        payTypeMap.put(OutChannel.unionpay.name(), "bdpay");

        payTypeMap.put(OutChannel.aliwap.name(), "alipaywap");
        payTypeMap.put(OutChannel.alih5.name(), "alipaycode");
        payTypeMap.put(OutChannel.wechatwap.name(), "wxwap");
        payTypeMap.put(OutChannel.wechath5.name(), "weixincode");
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "全通支付请求：{}", JSON.toJSONString(reqParams));
        //根据入参加工参数成通汇宝支付需要的参数与签名
        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        String dataParams = "p0_Cmd=" + params.get("p0_Cmd") + "&p1_MerId=" + params.get("p1_MerId") +
                "&p2_Order=" + params.get("p2_Order") + "&p3_Amt=" + params.get("p3_Amt") +
                "&p4_Cur=" + params.get("p4_Cur") + "&p5_Pid=" + params.get("p5_Pid")+
                "&p6_Pcat=" + params.get("p6_Pcat") + "&p7_Pdesc=" + params.get("p7_Pdesc") +
                "&p8_Url=" + params.get("p8_Url") + "&pa_MP=" + params.get("pa_MP") +
                "&pd_FrpId=" + params.get("pd_FrpId") + "&pr_NeedResponse=" + params.get("pr_NeedResponse") +
                "&hmac=" + params.get("hmac");
        if ("aliwap".equals(reqParams.getOutChannel()) || "wechatwap".equals(reqParams.getOutChannel())
                ||"alih5".equals(reqParams.getOutChannel()) || "wechath5".equals(reqParams.getOutChannel())
                || "unionquickpay".equals(reqParams.getOutChannel())){
            saveOrder(reqParams, mcpConfig.getUpMerchantNo());
            OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
            orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(payUrl, params));
            return orderApiRespParams;
        }else {
            String result = post(payUrl, dataParams);

            LogByMDC.info(channelNo, "全通支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);
            Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
            });
            String status = resultMap.get("status");
            Assert.isTrue("0".equals(status), "全通支付状态响应:" + resultMap.get("Msg"));
            String payUrl = resultMap.get("payImg");

            saveOrder(reqParams, mcpConfig.getUpMerchantNo());
            OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
            orderApiRespParams.setCode_url(payUrl);
            return orderApiRespParams;
        }

    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "全通支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        Map<String, String> params = new TreeMap<>();
        params.put("p0_Cmd", "Buy");
        params.put("p1_MerId", upMerchantNo);
        params.put("p2_Order", orderNo);
        params.put("p3_Amt", String.valueOf(new BigDecimal(amount)));
        params.put("p4_Cur", "CNY");
        params.put("p5_Pid", "test");

        params.put("p6_Pcat", "test");
        params.put("p7_Pdesc", "test");
        params.put("p8_Url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("pa_MP", "test");
        params.put("pd_FrpId", payType);
        params.put("pr_NeedResponse", "1");

        String buildParams = SignUtils.buildParams(params) ;
        String hmac = DigestUtil.hmacSign(buildParams, upPublicKey);
        params.put("hmac", hmac);
        return params;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "全通支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "全通支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "全通支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String amount = params.get("r3_Amt");
        String systemNo = params.get("r6_Order");
        String paystatus = params.get("r1_Code");
        if (!"1".equals(paystatus)) {
            LogByMDC.error(channelNo, "全通支付回调订单支付回调订单：{}，支付未成功，不再向下通知", systemNo);
            return "success";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(systemNo);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "全通支付异步回调：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            LogByMDC.error(channelNo, "全通支付异步回调：{}，下发通知失败", order.getOrderNo());
        }
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("hmac");
        String signParams = params.get("p1_MerId")+params.get("r0_Cmd")+params.get("r1_Code")+params.get("r2_TrxId")+
                params.get("r3_Amt")+params.get("r4_Cur")+params.get("r5_Pid")+params.get("r6_Order")+
                params.get("r7_Uid")+params.get("r8_MP")+params.get("r9_BType");
        LogByMDC.info(channelNo, "全通支付回调订单:{}，参与验签参数:{}", params.get("r6_Order"), signParams);

        String newSign = DigestUtil.hmacSign(signParams, upMerchantKey);
        return newSign.equals(sign);
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
}
