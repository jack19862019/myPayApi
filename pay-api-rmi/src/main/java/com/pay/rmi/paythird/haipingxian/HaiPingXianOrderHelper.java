package com.pay.rmi.paythird.haipingxian;

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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Component
public class HaiPingXianOrderHelper extends OrderApiFactory implements ParamsService, HttpService, ReturnDownService {

    public void init(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        this.channel = channel;
        this.mcpConfig = mcpConfig;
        this.reqParams = reqParams;
    }

    @Override
    public Map<String, String> requestToUpParams(OrderReqParams reqParams) {
        Optional<UpPayTypeEntity> upPayTypeEntity = channel.getUpPayTypes().stream()
                .filter(e -> e.getPayType().getPayTypeFlag().equals(reqParams.getOutChannel())).findFirst();
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "海平线不支持的支付方式:" + reqParams.getOutChannel());
        params.put("mch_id", mcpConfig.getUpMerchantNo());//商户ID
        params.put("order_sn", reqParams.getOrderNo());//订单ID
        params.put("money",  new DecimalFormat("#.00").format(new BigDecimal(reqParams.getAmount())));
        params.put("format", "json");
        params.put("time", getSecondTimestampTwo(new Date())+"");//时间戳
        params.put("ptype", upPayTypeEntity.get().getUpPayTypeFlag());//支付代码（支付方式）
        params.put("notify_url", getCallbackUrl());
        params.put("goods_desc", reqParams.getMemo());
        params.put("client_ip", reqParams.getReqIp());
        return params;
    }
    public static int getSecondTimestampTwo(Date date){
        if (null == date) {
            return 0;
        }
        String timestamp = String.valueOf(date.getTime()/1000);
        return Integer.valueOf(timestamp);
    }
    @Override
    public String signToUp(String context, String upKey) {
        return PayMD5.MD5Encode(context +"&key="+ mcpConfig.getUpKey());
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("code");
        org.springframework.util.Assert.isTrue("1".equals(code), "海平线支付状态响应:" + resultMap.get("msg"));

        String data = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        String order_sn = resultData.get("order_sn");
        org.springframework.util.Assert.isTrue(!StringUtils.isEmpty(order_sn), "海平线支付响应未返回订单号");
        String codeUrl = channel.getUpPayUrl()+"&a=info&osn="+order_sn;
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());

        orderApiRespParams.setCode_url(codeUrl);
        return orderApiRespParams;

    }

    @Override
    public String httpPost(Map<String, String> params) {
        String dataParams = "&mch_id=" + params.get("mch_id") + "&ptype=" + params.get("ptype") +
                "&order_sn=" + params.get("order_sn") + "&money=" + params.get("money") +
                "&goods_desc=" + params.get("goods_desc") + "&client_ip=" + params.get("client_ip")+
                "&format=" + params.get("format") + "&notify_url=" + params.get("notify_url") +
                "&time=" + params.get("time") + "&sign=" + params.get("sign");

        Map<String, String> head = new HashMap();
        head.put("Content-Type", "application/x-www-form-urlencoded");
        return HttpKit.post(channel.getUpPayUrl(), dataParams, head);
    }
}
