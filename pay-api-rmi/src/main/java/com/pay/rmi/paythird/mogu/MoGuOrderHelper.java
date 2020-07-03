package com.pay.rmi.paythird.mogu;

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
import com.pay.rmi.paythird.HttpService;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.ParamsService;
import com.pay.rmi.paythird.ReturnDownService;
import com.pay.rmi.paythird.kuailefu.util.HttpKit;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Component
public class MoGuOrderHelper extends OrderApiFactory implements ParamsService, HttpService, ReturnDownService {

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
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "快乐付不支持的支付方式:" + reqParams.getOutChannel());
        params.put("merchant_no", mcpConfig.getUpMerchantNo());
        params.put("method", "pay");
        params.put("out_trade_no", reqParams.getOrderNo());
        String timpstamp = System.currentTimeMillis() + "";
        params.put("timestamp", timpstamp);
        params.put("amount", String.valueOf(new BigDecimal(reqParams.getAmount()).multiply(new BigDecimal("100")).intValue()));
        params.put("body", "body");
        params.put("notify_url", getCallbackUrl());
        params.put("way",  upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("return_url", reqParams.getReturnUrl());
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        return Md5Utils.MD5(context + mcpConfig.getUpKey());
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());

        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(result, params));
        return orderApiRespParams;
    }
}
