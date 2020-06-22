package com.pay.rmi.paythird.jialian;

import com.pay.common.exception.Assert;
import com.pay.common.utils.DateUtil;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.UpPayTypeEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.BuildFormUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.ParamsService;
import com.pay.rmi.paythird.ReturnDownService;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;


@Component
public class JiaLianOrderHelper extends OrderApiFactory implements ParamsService, ReturnDownService {

    public void init(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        this.channel = channel;
        this.mcpConfig = mcpConfig;
        this.reqParams = reqParams;
    }

    @Override
    public Map<String, String> requestToUpParams(OrderReqParams reqParams) {
        Optional<UpPayTypeEntity> upPayTypeEntity = channel.getUpPayTypes().stream()
                .filter(e -> e.getPayType().getPayTypeFlag().equals(reqParams.getOutChannel())).findFirst();
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "嘉联不支持的支付方式:" + reqParams.getOutChannel());
        params.put("pay_memberid", mcpConfig.getUpMerchantNo());//商户ID
        params.put("pay_orderid", reqParams.getOrderNo());//订单ID
        params.put("pay_amount",  new DecimalFormat("#.00").format(new BigDecimal(reqParams.getAmount())));
        params.put("pay_applydate", DateUtil.toStr02(null, new Date()));//申请时间，格式：2018-01-02 01:30:40
        params.put("pay_bankcode", upPayTypeEntity.get().getUpPayTypeFlag());//支付代码（支付方式）
        params.put("pay_callbackurl", reqParams.getReturnUrl());
        params.put("pay_notifyurl", getCallbackUrl());//异步通知地址，如果不填则不通知
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        return PayMD5.MD5Encode(context +"&key="+ mcpConfig.getUpKey()).toUpperCase();
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(result, params));
        return orderApiRespParams;

    }
}
