package com.pay.rmi.paythird.quantong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.exception.Assert;
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
import com.pay.rmi.paythird.quantong.util.DigestUtil;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Component
public class QuanTongOrderHelper extends OrderApiFactory implements ParamsService, HttpService, ReturnDownService {

    public void init(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        this.channel = channel;
        this.mcpConfig = mcpConfig;
        this.reqParams = reqParams;
    }

    @Override
    public Map<String, String> requestToUpParams(OrderReqParams reqParams) {
        Optional<UpPayTypeEntity> upPayTypeEntity = channel.getUpPayTypes().stream()
                .filter(e -> e.getPayType().getPayTypeFlag().equals(reqParams.getOutChannel())).findFirst();
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "全通不支持的支付方式:" + reqParams.getOutChannel());
        params.put("p0_Cmd", "Buy");
        params.put("p1_MerId", mcpConfig.getUpMerchantNo());
        params.put("p2_Order", reqParams.getOrderNo());
        params.put("p3_Amt", String.valueOf(new BigDecimal(reqParams.getAmount())));
        params.put("p4_Cur", "CNY");
        params.put("p5_Pid", "test");

        params.put("p6_Pcat", "test");
        params.put("p7_Pdesc", "test");
        params.put("p8_Url", getCallbackUrl());
        params.put("pa_MP", "test");
        params.put("pd_FrpId", upPayTypeEntity.get().getUpPayTypeFlag());
        params.put("pr_NeedResponse", "1");
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        return DigestUtil.hmacSign(context, mcpConfig.getUpKey());
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        if ("aliwap".equals(reqParams.getOutChannel()) || "wechatwap".equals(reqParams.getOutChannel())
                ||"alih5".equals(reqParams.getOutChannel()) || "wechath5".equals(reqParams.getOutChannel())
                || "unionquickpay".equals(reqParams.getOutChannel())){
            orderApiRespParams.setPay_form(BuildFormUtils.buildSubmitForm(result, params));
            return orderApiRespParams;
        }else{
            Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
            });
            String code = resultMap.get("status");
            Assert.mustBeTrue("0".equals(code), "和记上游支付状态响应:" + resultMap.get("Msg"));
            String payUrl = resultMap.get("payImg");
            orderApiRespParams.setCode_url(payUrl);
            return orderApiRespParams;
        }
    }

    @Override
    public String httpPost(Map<String, String> params) {
        String dataParams = "p0_Cmd=" + params.get("p0_Cmd") + "&p1_MerId=" + params.get("p1_MerId") +
                "&p2_Order=" + params.get("p2_Order") + "&p3_Amt=" + params.get("p3_Amt") +
                "&p4_Cur=" + params.get("p4_Cur") + "&p5_Pid=" + params.get("p5_Pid")+
                "&p6_Pcat=" + params.get("p6_Pcat") + "&p7_Pdesc=" + params.get("p7_Pdesc") +
                "&p8_Url=" + params.get("p8_Url") + "&pa_MP=" + params.get("pa_MP") +
                "&pd_FrpId=" + params.get("pd_FrpId") + "&pr_NeedResponse=" + params.get("pr_NeedResponse") +
                "&hmac=" + params.get("hmac");
        Map<String, String> head = new HashMap();
        head.put("Content-Type", "application/x-www-form-urlencoded");
        return HttpKit.post(channel.getUpPayUrl(), dataParams, head);
    }
}
