package com.pay.rmi.paythird.xiami;

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
import com.pay.rmi.common.utils.*;
import com.pay.rmi.pay.constenum.OutChannel;
import com.pay.rmi.paythird.AbstractPay;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

@Service(XiaMi.channelNo)
public class XiaMi extends AbstractPay {
    public static final String channelNo = "xiami";

    static final String payUrl = "http://api.xshljjd.com:5555/";

    private static final Map<String, String> payTypeMap = new HashMap<>();

    public XiaMi() {
        payTypeMap.put(OutChannel.alipay.name(), "alipayqrcode");//支付宝扫码支付
        payTypeMap.put(OutChannel.wechatpay.name(), "wxpayqrcode");//微信扫码支付
        payTypeMap.put(OutChannel.aliwap.name(), "alipaytrans");//支付宝转账
        payTypeMap.put(OutChannel.zk.name(), "alipay2bank");//支付宝转银行卡
    }

    @Override
    public OrderApiRespParams order(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        LogByMDC.info(channelNo, "虾米支付，请求：{}", JSON.toJSONString(reqParams));

        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        String payType = payTypeMap.get(reqParams.getOutChannel());
        Assert.notNull(payType, "虾米支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        String je = String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue());
        String clientip = reqParams.getReqIp();
        String timestamp = System.currentTimeMillis()+"";
        String buildParams =upMerchantNo+orderNo+je+timestamp+upPublicKey;
        String sign = Objects.requireNonNull(Md5Utils.MD5(buildParams));

        String params ="title="+orderNo+"&amount="+je+"&clientip="+clientip+"&timestamp="+timestamp+"&sign="+sign+"&callback="+getCallbackUrl(channelNo, merchNo, orderNo);
        String result = sendGet(payUrl + "api/"+upMerchantNo+"/"+payType,params);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String ret = resultMap.get("ret");
        Assert.isTrue("true".equals(ret), "虾米支付状态响应:" + resultMap.get("msg"));

        String data = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        String payUrl = resultData.get("payUrl");

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(payUrl);
        return orderApiRespParams;
    }


    @Override
    public String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        LogByMDC.info(channelNo, "虾米支付异步回调内容：{}", JSON.toJSONString(params));

        if (order.getOrderStatus() == OrderStatus.succ) {
            LogByMDC.error(channelNo, "虾米支付异步回调订单：{}，重复回调", order.getOrderNo());
            return "true";
        }

        String upMerchantKey = mcpConfig.getUpKey();
        boolean signVerify = verifySignParams(params, upMerchantKey);
        if (!signVerify) {
            LogByMDC.error(channelNo, "虾米支付回调订单：{}，回调验签失败", order.getOrderNo());
            return "fail";
        }

        String data = params.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        String amount = resultData.get("amount");
        String systemNo = resultData.get("title");

        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));
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
        return "true";
    }

    private boolean verifySignParams(Map<String, String> params, String upMerchantKey) {
        String sign = params.remove("sign");
        String title = params.remove("title");
        String signParams = params.get("data");
        LogByMDC.info(channelNo, "虾米支付回调订单:{}，参与验签参数:{}", title, signParams);

        String buildParams =signParams+upMerchantKey;
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams));
        return newSign.equals(sign);
    }


    public static String sendGet(String url, String param){
        String result = "";
        String urlName = url + "?" + param;
        try{
            URL realUrl = new URL(urlName);
            //打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //建立实际的连接
            conn.connect();
            //获取所有的响应头字段
            Map<String, List<String>> map = conn.getHeaderFields();
            //遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "-->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
