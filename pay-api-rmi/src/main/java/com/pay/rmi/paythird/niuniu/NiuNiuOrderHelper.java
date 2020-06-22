package com.pay.rmi.paythird.niuniu;

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
public class NiuNiuOrderHelper extends OrderApiFactory implements ParamsService, HttpService, ReturnDownService {

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
        params.put("login_id", mcpConfig.getUpMerchantNo());//商户号
        params.put("pay_type", upPayTypeEntity.get().getUpPayTypeFlag());//签名类型
        params.put("create_ip", reqParams.getReqIp());//客户端IP
        params.put("sign_type", "MD5");//签名类型
        params.put("order_type", "1");//订单类型
        params.put("order_sn", reqParams.getOrderNo());//商户订单编号
        params.put("nonce", System.currentTimeMillis() + "");//随机数
        params.put("send_currency", "cny");//货币类型
        params.put("recv_currency", "cny");//货币类型
        params.put("notify_url", getCallbackUrl());//后台通知回調地址
        params.put("amount", new DecimalFormat("#.00").format(new BigDecimal(reqParams.getAmount())));
        String time = System.currentTimeMillis() + "";
        params.put("create_time", time.substring(0, 10));//请求时间
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        return PayMD5.MD5Encode(context +"&api_secret="+ mcpConfig.getUpKey());
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String code = resultMap.get("code");
        Assert.mustBeTrue("0".equals(code), "牛牛支付状态响应:" + resultMap.get("msg"));
        String http_url = resultMap.get("http_url");
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        orderApiRespParams.setCode_url(http_url);
        return orderApiRespParams;

    }

    @Override
    public String httpPost(Map<String, String> params) {
        Map<String, String> head = new HashMap();
        head.put("Content-Type", "application/json");
        return HttpKit.post(channel.getUpPayUrl(), JSON.toJSONString(this.params), head);
    }
}
