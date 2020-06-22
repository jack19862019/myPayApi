package com.pay.rmi.paythird.heji;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.exception.Assert;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.UpPayTypeEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.paythird.HttpService;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.ParamsService;
import com.pay.rmi.paythird.ReturnDownService;
import com.pay.rmi.paythird.kuailefu.util.HttpKit;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;


@Component
public class HeJiOrderHelper extends OrderApiFactory implements ParamsService, HttpService, ReturnDownService {

    public void init(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        this.channel = channel;
        this.mcpConfig = mcpConfig;
        this.reqParams = reqParams;
    }

    @Override
    public Map<String, String> requestToUpParams(OrderReqParams reqParams) {
        Optional<UpPayTypeEntity> upPayTypeEntity = channel.getUpPayTypes().stream()
                .filter(e -> e.getPayType().getPayTypeFlag().equals(reqParams.getOutChannel())).findFirst();
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "和记不支持的支付方式:" + reqParams.getOutChannel());
        params.put("mchOrderNo", reqParams.getOrderNo());
        params.put("mchId", mcpConfig.getUpMerchantNo());
        params.put("appId", "0ede612e17c444f987c0c488f4c466e4");//应用id
        params.put("notifyUrl", getCallbackUrl());
        params.put("productId", upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("currency", "cny");//币种
        params.put("subject", "subject");
        params.put("body", "body");
        params.put("extra", "1");

        params.put("amount", String.valueOf(new BigDecimal(reqParams.getAmount()).multiply(new BigDecimal("100")).intValue()));
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        return PayMD5.MD5Encode(context +"&key="+ mcpConfig.getUpKey()).toUpperCase();
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("retCode");
        Assert.mustBeTrue("SUCCESS".equals(code), "和记上游支付状态响应:" + resultMap.get("retMsg"));
        String data = resultMap.get("payParams");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        String payUrl = resultData.get("codeUrl");
        orderApiRespParams.setCode_url(payUrl);

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        return orderApiRespParams;

    }

    @Override
    public String httpPost(Map<String, String> params) {
        Map<String, String> head = new HashMap();
        head.put("Content-Type", "application/json");
        return HttpKit.post(channel.getUpPayUrl(), JSON.toJSONString(this.params), head);
    }
}
