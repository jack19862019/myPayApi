package com.pay.rmi.paythird.katong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mysema.commons.lang.URLEncoder;
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
import com.pay.rmi.paythird.katong.util.KaTongUtil;
import com.pay.rmi.paythird.kuailefu.util.HttpKit;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Component
public class KaTongOrderHelper extends OrderApiFactory implements ParamsService, HttpService, ReturnDownService {

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
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "卡通不支持的支付方式:" + reqParams.getOutChannel());
        params.put("orderNo", reqParams.getOrderNo() );
        params.put("payMode", upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("merchantNo",mcpConfig.getUpMerchantNo() );
        params.put("amount", String.valueOf(new BigDecimal(reqParams.getAmount()).multiply(new BigDecimal("100")).intValue()));
        params.put("notifyUrl",  getCallbackUrl());
        params.put("returnUrl", reqParams.getReturnUrl());
        params.put("ts", Instant.now().getEpochSecond()+"");

        String signReduce = KaTongUtil.generateSignReduce(params);
        String sign =KaTongUtil.encodeMD5(signReduce + "&key=" + mcpConfig.getUpKey());
        params.put("sign", sign);
        params.put("signReduce", signReduce);
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        return PayMD5.MD5Encode(context + mcpConfig.getUpKey()).toLowerCase();
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(result);
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        return orderApiRespParams;
    }
}
