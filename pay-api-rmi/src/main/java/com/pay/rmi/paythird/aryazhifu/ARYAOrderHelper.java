package com.pay.rmi.paythird.aryazhifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.exception.Assert;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.UpPayTypeEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.HttpService;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.ParamsService;
import com.pay.rmi.paythird.ReturnDownService;
import com.pay.rmi.paythird.kuailefu.util.HttpKit;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;


@Component
public class ARYAOrderHelper extends OrderApiFactory implements ParamsService, ReturnDownService, HttpService {

    public void init(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        this.channel = channel;
        this.mcpConfig = mcpConfig;
        this.reqParams = reqParams;
    }

    @Override
    public Map<String, String> requestToUpParams(OrderReqParams reqParams) {
        Optional<UpPayTypeEntity> upPayTypeEntity = channel.getUpPayTypes().stream()
                .filter(e -> e.getPayType().getPayTypeFlag().equals(reqParams.getOutChannel())).findFirst();
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "arya支付不支持的支付方式:" + reqParams.getOutChannel());
        params.put("uid", mcpConfig.getUpMerchantNo());
        params.put("money", new DecimalFormat("#.00").format(new BigDecimal(reqParams.getAmount())));
        params.put("channel", upPayTypeEntity.get().getUpPayTypeName());
        params.put("outTradeNo", reqParams.getOrderNo());
        params.put("returnUrl", reqParams.getReturnUrl());
        params.put("notifyUrl",  getCallbackUrl());
        params.put("timestamp", System.currentTimeMillis()+"");
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        // 签名
        Map<String, String> paramMap4Sign = new TreeMap<>();
        paramMap4Sign.put("token",upKey);
        paramMap4Sign.put("uid", params.get("uid"));
        paramMap4Sign.put("money", params.get("money"));
        paramMap4Sign.put("channel", params.get("channel"));
        paramMap4Sign.put("outTradeNo", params.get("outTradeNo"));
        paramMap4Sign.put("returnUrl", params.get("returnUrl"));
        paramMap4Sign.put("notifyUrl",  params.get("notifyUrl"));
        paramMap4Sign.put("timestamp", params.get("timestamp"));
        return PayMD5.MD5Encode(SignUtils.buildParams(paramMap4Sign)).toUpperCase();
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);

        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("code");
        org.springframework.util.Assert.isTrue("0".equals(code), "arya云支付状态响应:" + resultMap.get("msg"));

        String data = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        String payUrl = resultData.get("payUrl");
        org.springframework.util.Assert.isTrue(!StringUtils.isEmpty(payUrl), "arya云支付响应未返回支付二维码");

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());

        orderApiRespParams.setCode_url(payUrl);
        return orderApiRespParams;
    }

    @Override
    public String httpPost(Map<String, String> params) {
        Map<String, String> head = new HashMap();
        head.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        return HttpKit.post(channel.getUpPayUrl(), SignUtils.buildParams(this.params), head);
    }
}
