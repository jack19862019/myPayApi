package com.pay.rmi.paythird.xincheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mysema.commons.lang.URLEncoder;
import com.pay.common.exception.Assert;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.UpPayTypeEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.HttpService;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.ParamsService;
import com.pay.rmi.paythird.ReturnDownService;
import com.pay.rmi.paythird.kuailefu.util.HttpKit;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Component
public class XinChengOrderHelper extends OrderApiFactory implements ParamsService, HttpService, ReturnDownService {

    public void init(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        this.channel = channel;
        this.mcpConfig = mcpConfig;
        this.reqParams = reqParams;
    }

    @Override
    public String httpPost(Map<String, String> params) {
        Map<String, String> head = new HashMap();
        head.put("Content-Type", "application/json");
        return HttpKit.post(channel.getUpPayUrl(), JSON.toJSONString(this.params), head);
    }

    @Override
    public Map<String, String> requestToUpParams(OrderReqParams reqParams) {
        Optional<UpPayTypeEntity> upPayTypeEntity = channel.getUpPayTypes().stream()
                .filter(e -> e.getPayType().getPayTypeFlag().equals(reqParams.getOutChannel())).findFirst();
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "新城不支持的支付方式:" + reqParams.getOutChannel());
        params.put("CreateTradeNo", reqParams.getOrderNo());
        params.put("UserId", mcpConfig.getUpMerchantNo());
        params.put("PayTimes", (System.currentTimeMillis()+"").substring(0, 10));
        params.put("ReturnUrl", reqParams.getReturnUrl());
        params.put("NotifyUrl", getCallbackUrl());
        params.put("BankCode", upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("ShopNames", "test");
        params.put("CreateMoney", new DecimalFormat("#.00").format(new BigDecimal(reqParams.getAmount())));
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        return PayMD5.MD5Encode(context +"&key="+ mcpConfig.getUpKey()).toUpperCase();
    }

    @Override
    public OrderApiRespParams returnDown(String resultOrPayUrl) {
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());

        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(result, params));
        return orderApiRespParams;
    }

    public static void main(String[] args) {
        String str= "{\"PayStatus\":\"Order_SUCCESS\",\"UserId\":\"1750\",\"NotifyUrl\":\"http://47.56.19.236:10086/pay/callback/xincheng/SH-538700002/1590826326598\",\"ShopNames\":\"test\",\"CreateMoney\":\"300.00\",\"ReturnUrl\":\"http://www.google.com\",\"Sign\":\"805AAF429173751A02AD573F361CA58F\",\"PayTimes\":\"-62170012800\",\"CreateTradeNo\":\"1590826326598\"}";
        Map<String, String> resultMap1 = JSON.parseObject(str, new TypeReference<Map<String, String>>() {
        });
        resultMap1.remove("Sign");
        String signData = StrKit.formatSignData(resultMap1);

        String s = PayMD5.MD5Encode(signData + "&key=924Ui9Fsd99nf32NVsM590nyw29352nt").toUpperCase();
        System.out.println("************88"+s);

    }
}
