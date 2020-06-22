package com.pay.rmi.paythird.junlintianxia;

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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Component
public class JunLinTianXiaOrderHelper extends OrderApiFactory implements ParamsService, HttpService, ReturnDownService {

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
        params.put("mch_id", mcpConfig.getUpMerchantNo());//商户号
        params.put("pay_type", upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("out_trade_no", reqParams.getOrderNo());//商户订单编号
        params.put("user_code", "123456");
        params.put("notify_url",getCallbackUrl());//后台通知回調地址
        params.put("order_amount", new DecimalFormat("#.00").format(new BigDecimal(reqParams.getAmount())));
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        return PayMD5.MD5Encode(context + mcpConfig.getUpKey()).toUpperCase();
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("success");
        Assert.mustBeTrue("0".equals(code), "君临天下支付状态响应:" + resultMap.get("msg"));
        String payUrl = resultMap.get("pay_url");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        orderApiRespParams.setCode_url(payUrl);
        return orderApiRespParams;

    }

    @Override
    public String httpPost(Map<String, String> params) {
        Map<String, String> head = new HashMap();
        head.put("Content-Type", "application/json");
        return HttpKit.post(channel.getUpPayUrl(), JSON.toJSONString(this.params), head);
    }
}
