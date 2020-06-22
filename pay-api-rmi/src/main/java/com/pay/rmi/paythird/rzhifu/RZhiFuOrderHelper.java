package com.pay.rmi.paythird.rzhifu;
import com.pay.common.exception.Assert;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;


@Component
public class RZhiFuOrderHelper extends OrderApiFactory implements ParamsService, ReturnDownService {

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        params.put("api_code", mcpConfig.getUpMerchantNo());
        params.put("is_type",  upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("order_id", reqParams.getOrderNo());
        params.put("return_url", reqParams.getReturnUrl());
        params.put("notify_url", getCallbackUrl());
        params.put("return_type", "json");
        params.put("price",  new DecimalFormat("#.00").format(new BigDecimal(reqParams.getAmount())));
        params.put("time", sdf.format(new Date()));
        params.put("mark", reqParams.getMemo());
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
