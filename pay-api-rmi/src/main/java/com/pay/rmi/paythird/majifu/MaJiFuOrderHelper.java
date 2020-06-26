package com.pay.rmi.paythird.majifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.exception.Assert;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.UpPayTypeEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.HttpsParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.HttpService;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.ParamsService;
import com.pay.rmi.paythird.ReturnDownService;
import com.pay.rmi.paythird.kuailefu.util.HttpKit;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Component
public class MaJiFuOrderHelper extends OrderApiFactory implements ParamsService, ReturnDownService, HttpService {

    @Autowired
    RestTemplate restTemplate;

    public void init(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        this.channel = channel;
        this.mcpConfig = mcpConfig;
        this.reqParams = reqParams;
    }

    @Override
    public Map<String, String> requestToUpParams(OrderReqParams reqParams) {
        Optional<UpPayTypeEntity> upPayTypeEntity = channel.getUpPayTypes().stream()
                .filter(e -> e.getPayType().getPayTypeFlag().equals(reqParams.getOutChannel())).findFirst();
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "麻吉付不支持的支付方式:" + reqParams.getOutChannel());
        String payType = upPayTypeEntity.get().getUpPayTypeFlag();
        String upMerchantNo = mcpConfig.getUpMerchantNo();
        String upPublicKey = mcpConfig.getUpKey();
        org.springframework.util.Assert.notNull(payType, "麻吉付支付不支持的支付方式:" + reqParams.getOutChannel());
        String merchNo = reqParams.getMerchNo();
        String orderNo = reqParams.getOrderNo();
        String amount = reqParams.getAmount();

        String stamp = System.currentTimeMillis() + "";
        stamp = stamp.substring(0, 10);

        Map<String, String> params = new TreeMap<>();
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8:00");
        SimpleDateFormat format01 = new SimpleDateFormat("yyyyMMdd");
        format01.setTimeZone(timeZone);
        SimpleDateFormat format02 = new SimpleDateFormat("HHmmss");
        format02.setTimeZone(timeZone);
        SimpleDateFormat format03 = new SimpleDateFormat("yyyyMMddHHmmss");
        format03.setTimeZone(timeZone);

        params.put("txnType", "01");//报文类型
        params.put("txnSubType", payType);//支付代码（支付方式）
        params.put("secpVer", "icp3-1.1");//安全协议版本
        params.put("secpMode", "perm");//安全协议类型
        params.put("macKeyId", upMerchantNo);//密钥识别
        params.put("orderDate", format01.format(new Date()));//下单日期
        params.put("orderTime", format02.format(new Date()));//下单时间
        params.put("merId", upMerchantNo);//商户代号
        params.put("orderId", orderNo);//商户订单号
        params.put("pageReturnUrl", reqParams.getReturnUrl());//交易结果页面通知地址
        params.put("notifyUrl", getCallbackUrl());//交易结果后台通知地址
        params.put("productTitle", "apple");//商品名称
        params.put("txnAmt", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal("100")).intValue()));//交易金额
        params.put("currencyCode", "156");//交易币种
        params.put("timeStamp", format03.format(new Date()));//时间戳
        if ("42".equals(payType)){
            params.put("clientIp", reqParams.getReqIp());//客户端ip
            params.put("sceneBizType", "ANDROID_APP");//场景业务类型
            params.put("appName", "qipaigame");//应用名
            params.put("appPackage", "qipaigame");//应用包名
        }
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        return PayMD5.MD5Encode(context +"&k="+ mcpConfig.getUpKey());
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {});
        String status = resultMap.get("respCode");
        org.springframework.util.Assert.isTrue("0000".equals(status), "麻吉付上游支付状态响应:" + resultMap.get("respMsg"));
        String qrCode = "";
        if (resultMap.containsKey("codeImgUrl")) {
            qrCode = resultMap.get("codeImgUrl");
        }
        if (resultMap.containsKey("codePageUrl")) {
            qrCode = resultMap.get("codePageUrl");
        }

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(qrCode);
        return orderApiRespParams;
    }

    @Override
    public String httpPost(Map<String, String> params) {
        return restTemplate.postForObject(channel.getUpPayUrl(), HttpsParams.buildFormEntity(params), String.class);
    }
}
