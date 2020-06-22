package com.pay.rmi.paythird.xunke;

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
import com.pay.rmi.paythird.xunke.util.EkaPayEncrypt;
import com.pay.rmi.paythird.xunke.util.HttpUtil;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;


@Component
public class XunKeOrderHelper extends OrderApiFactory implements ParamsService, HttpService, ReturnDownService {

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
        params.put("parter", mcpConfig.getUpMerchantNo());
        params.put("md5key", mcpConfig.getUpKey());
        params.put("type", upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("merchNo", reqParams.getMerchNo());
        params.put("orderid", reqParams.getOrderNo());
        params.put("callbackurl", getCallbackUrl());
        params.put("value", String.valueOf(new BigDecimal(reqParams.getAmount()).intValue()));
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        String sign = EkaPayEncrypt.EkaPayBankMd5Sign(params.get("type"),params.get("parter"),params.get("value"),params.get("orderid"),params.get("callbackurl"),params.get("md5key"));//签名
        return sign;
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);

        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("code");
        Assert.mustBeTrue("1".equals(code), "讯科支付状态响应:" + resultMap.get("msg"));
        String qrcode = resultMap.get("qrcode");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        orderApiRespParams.setCode_url(qrcode);
        return orderApiRespParams;

    }

    @Override
    public String httpPost(Map<String, String> params) {
        String dataParams = "parter="+params.get("parter")+"&type="+params.get("type")+"&value="+params.get("value")+"&orderid="+params.get("orderid")+"&callbackurl="+params.get("callbackurl")
                +"&sign="+params.get("sign")+"&onlyqrcode=1";
        String type =params.get("type");
        String url = "";
        if(type.equals("992") || type.equals("1006")){
            url=channel.getUpPayUrl() + "alipayBank.aspx";
        }else if(type.equals("1007") || type.equals("1004")){
            url=channel.getUpPayUrl() + "wxpayBank.aspx";
        }else if(type.equals("1005")){
            url=channel.getUpPayUrl() + "unionpayBank.aspx";
        }else{
            url=channel.getUpPayUrl() + "ChargeBank.aspx";
        }
        String result = HttpUtil.sendGetHttp(url, dataParams);
        return result;
    }
}
