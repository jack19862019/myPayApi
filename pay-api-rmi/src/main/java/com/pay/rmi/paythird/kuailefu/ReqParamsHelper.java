package com.pay.rmi.paythird.kuailefu;

import com.pay.common.exception.Assert;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.UpPayTypeEntity;
import com.pay.data.params.OrderReqParams;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ReqParamsHelper {

    public Map<String, String> requestToUpParams(ChannelEntity channelEntity, McpConfigEntity mcpConfig, OrderReqParams reqParams, String callbackUrl) {
        Optional<UpPayTypeEntity> upPayTypeEntity = channelEntity.getUpPayTypes().stream()
                .filter(e -> e.getPayType().getPayTypeFlag().equals(reqParams.getOutChannel())).findFirst();
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "快乐付不支持的支付方式:" + reqParams.getOutChannel());
        Map<String, String> params = new HashMap<String, String>();
        params.put("merchantNo", mcpConfig.getUpMerchantNo());
        params.put("version", "V2");
        params.put("signType", "MD5");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("date", formatter.format(new Date()));
        params.put("channleType", upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("orderNo", reqParams.getOrderNo());
        params.put("bizAmt", new BigDecimal(reqParams.getAmount()) + "");
        params.put("noticeUrl", callbackUrl);
        params.put("accName", "张三");
        params.put("cardNo", "6230520080090842211");
        return params;
    }
}
