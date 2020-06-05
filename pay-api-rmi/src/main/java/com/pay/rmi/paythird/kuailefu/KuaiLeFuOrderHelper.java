package com.pay.rmi.paythird.kuailefu;

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
public class KuaiLeFuOrderHelper extends OrderApiFactory implements ParamsService, HttpService, ReturnDownService {

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
        params.put("merchantNo", mcpConfig.getUpMerchantNo());
        params.put("version", "V2");
        params.put("signType", "MD5");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("date", formatter.format(new Date()));
        params.put("channleType", upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("orderNo", reqParams.getOrderNo());
        params.put("bizAmt", new BigDecimal(reqParams.getAmount()) + "");
        params.put("noticeUrl", getCallbackUrl());
        params.put("accName", "张三");
        params.put("cardNo", "6230520080090842211");
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        return PayMD5.MD5Encode(context + mcpConfig.getUpKey()).toLowerCase();
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("detail");
        Assert.mustBeTrue("0".equals(resultMap.get("code")), "快乐付上游支付状态响应:" + resultMap.get("msg"));
        Map<String, String> resultMap1 = JSON.parseObject(code, new TypeReference<Map<String, String>>() {
        });
        if (resultMap1.get("PayURL") != null && !resultMap1.get("PayURL").equals("")) {
            StringBuilder sb = new StringBuilder();
            String payURL = resultMap1.get("PayURL");
            String domain = payURL.split("\\?")[0];
            String url = payURL.split("\\?")[1];
            StringBuilder append = sb.append(domain).append("?").append(URLEncoder.encodeURL(url));
            com.pay.common.exception.Assert.isEmpty("快乐付返回支付地址为空", append);
            orderApiRespParams.setCode_url(append.toString());
        }
        if (resultMap1.get("PayHtml") != null && !resultMap1.get("PayHtml").equals("")) {
            orderApiRespParams.setPay_form(resultMap1.get("PayHtml"));
        }
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        return orderApiRespParams;
    }
}
