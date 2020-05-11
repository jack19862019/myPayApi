package com.pay.rmi.paythird.haipingxian;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.HttpsParams;
import com.pay.rmi.common.utils.LogByMDC;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 海平线支付
 */
@Service(HaiPingXian.channelNo)
public class HaiPingXian extends AbstractPay {

    static final String channelNo = "haipingxian";

    private static final String payUrl = "http://hp.angeltutu.net/?c=Pay";

    private final Map<String, String> payTypeMap = new HashMap<>();

    public HaiPingXian() {
        payTypeMap.put(OutChannel.aliyssm.name(), "1");//支付宝扫码支付
    }


    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "海平线支付请求：{}", JSON.toJSONString(reqParams));

        Map<String, String> params = getParamsMap(mcpConfig, reqParams);
        String dataParams = "&mch_id=" + params.get("mch_id") + "&ptype=" + params.get("ptype") +
                "&order_sn=" + params.get("order_sn") + "&money=" + params.get("money") +
                "&goods_desc=" + params.get("goods_desc") + "&client_ip=" + params.get("client_ip")+
                "&format=" + params.get("format") + "&notify_url=" + params.get("notify_url") +
                "&time=" + params.get("time") + "&sign=" + params.get("sign");


        String result = post(payUrl,dataParams);
        LogByMDC.info(channelNo, "海平线支付响应 订单：{}，response：{}", reqParams.getOrderNo(), result);

        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("code");
        Assert.isTrue("1".equals(code), "海平线支付状态响应:" + resultMap.get("msg"));

        String data = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        String order_sn = resultData.get("order_sn");
        Assert.isTrue(!StringUtils.isEmpty(order_sn), "海平线支付响应未返回订单号");
        String codeUrl = payUrl+"&a=info&osn="+order_sn;
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(codeUrl);
        return orderApiRespParams;
    }

    private Map<String, String> getParamsMap(McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        String payType = payTypeMap.get(reqParams.getOutChannel());
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        Assert.notNull(payType, "海平线支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();
        Map<String, String> params = new TreeMap<>();

        params.put("mch_id", upMerchantNo);//商户ID
        params.put("order_sn", orderNo);//订单ID
        params.put("money",  new DecimalFormat("#.00").format(new BigDecimal(amount)));
        params.put("format", "json");
        params.put("time", getSecondTimestampTwo(new Date())+"");//时间戳
        params.put("ptype", payType);//支付代码（支付方式）
        params.put("notify_url", getCallbackUrl(channelNo, merchNo, orderNo));
        params.put("goods_desc", reqParams.getMemo());
        params.put("client_ip", reqParams.getReqIp());
        String sign = Md5Utils.MD5(getSignStr(params, upPublicKey));
        assert sign != null;
        params.put("sign", sign);
        return params;
    }

    public static int getSecondTimestampTwo(Date date){
        if (null == date) {
            return 0;
        }
        String timestamp = String.valueOf(date.getTime()/1000);
        return Integer.valueOf(timestamp);
    }

    private String getSignStr(Map<String, String> params, String upPublicKey) {
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upPublicKey;
        LogByMDC.info(channelNo, "海平线支付参与加签内容：{}", signParams);
        return signParams;
    }

    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "海平线支付回调内容：{}", params);
        if (order.getOrderStatus().equals(OrderStatus.succ)) {
            LogByMDC.error(channelNo, "嘉联支付回调订单：{}，重复回调", order.getOrderNo());
            return "success";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        //验签
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "海平线支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String trade_no = params.get("sh_order");
        String trade_status = params.get("status");
        String amount = params.get("money");

        if (!"success".equals(trade_status)) {
            LogByMDC.error(channelNo, "海平线支付回调订单：{}，支付未成功，不再向下通知", trade_no);
            return "fail";
        }

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
            LogByMDC.info(channelNo, "海平线支付回调订单：{}，下发通知成功", order.getOrderNo());
        } catch (Exception e) {
            LogByMDC.error(channelNo, "海平线支付回调订单：{}，下发通知失败{}", order.getOrderNo(), e.getMessage());
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return "success";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String signParams = SignUtils.buildParams(params, true) + "&key=" + upMerchantKey;
        LogByMDC.info(channelNo, "海平线支付回调订单:{}，参与验签参数:{}", params.get("orderId"), signParams);
        String newSign = Md5Utils.MD5(signParams);
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
