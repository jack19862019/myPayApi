package com.pay.rmi.paythird.tengda;

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
import java.util.Map;
import java.util.Optional;


@Component
public class TengDaOrderHelper extends OrderApiFactory implements ParamsService, ReturnDownService {

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
        params.put("shid", mcpConfig.getUpMerchantNo());//商户号
        params.put("bb", "1.0");
        String payType = upPayTypeEntity.get().getUpPayTypeFlag();
        params.put("zftd", payType);//签名类型
        params.put("ddh", reqParams.getOrderNo());//商户订单编号
        params.put("je", new DecimalFormat("#.00").format(new BigDecimal(reqParams.getAmount())));
        params.put("ddmc", "666");
        params.put("ddbz", "666");
        if ("wangyin".equals(payType)) {
            params.put("yhdm", reqParams.getBankCode());
        }
        params.put("ybtz",getCallbackUrl());//后台通知回調地址
        params.put("tbtz", reqParams.getReturnUrl());//后台通知回調地址
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        String buildParams ="shid="+params.get("shid")+"&bb=1.0&zftd="+params.get("zftd")+"&ddh="+params.get("ddh")+"&je="+params.get("je")+"&ddmc=666&ddbz=666&ybtz="+params.get("ybtz")+"&tbtz="+params.get("tbtz")+"&"+upKey;
        return PayMD5.MD5Encode(buildParams);
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(result, params));
        return orderApiRespParams;

    }
}
