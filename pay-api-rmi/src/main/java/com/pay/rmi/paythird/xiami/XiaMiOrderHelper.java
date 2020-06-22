package com.pay.rmi.paythird.xiami;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Component
public class XiaMiOrderHelper extends OrderApiFactory implements ParamsService, HttpService, ReturnDownService {

    public void init(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        this.channel = channel;
        this.mcpConfig = mcpConfig;
        this.reqParams = reqParams;
    }

    @Override
    public Map<String, String> requestToUpParams(OrderReqParams reqParams) {
        Optional<UpPayTypeEntity> upPayTypeEntity = channel.getUpPayTypes().stream()
                .filter(e -> e.getPayType().getPayTypeFlag().equals(reqParams.getOutChannel())).findFirst();
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "虾米不支持的支付方式:" + reqParams.getOutChannel());
        params.put("upMerchantNo", mcpConfig.getUpMerchantNo());
        params.put("payType", upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("merchNo", reqParams.getMerchNo());
        params.put("orderNo", reqParams.getOrderNo());
        params.put("je", String.valueOf(new BigDecimal(reqParams.getAmount()).multiply(new BigDecimal("100")).intValue()));
        params.put("clientip", reqParams.getReqIp());
        params.put("timestamp", System.currentTimeMillis()+"");
        params.put("callback", getCallbackUrl());
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        String orderNo = params.get("orderNo");
        String je = params.get("je");
        String timestamp = params.get("timestamp");

        String buildParams =mcpConfig.getUpMerchantNo()+orderNo+je+timestamp+upKey;
        return PayMD5.MD5Encode(buildParams);
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String ret = resultMap.get("ret");
        org.springframework.util.Assert.isTrue("true".equals(ret), "虾米支付状态响应:" + resultMap.get("msg"));

        String data = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        String payUrl = resultData.get("payUrl");

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        orderApiRespParams.setCode_url(payUrl);
        return orderApiRespParams;

    }

    @Override
    public String httpPost(Map<String, String> params) {
        String dataParams ="title="+params.get("orderNo")+"&amount="+params.get("je")+"&clientip="+params.get("clientip")+"&timestamp="+params.get("timestamp")+"&sign="+params.get("sign")+"&callback="+params.get("callback");
        String url = channel.getUpPayUrl() + "api/"+params.get("upMerchantNo")+"/"+params.get("payType");
        Map<String, String> head = new HashMap();
        head.put("Content-Type", "application/json");
        return HttpKit.post(url, dataParams, head);
    }
}
