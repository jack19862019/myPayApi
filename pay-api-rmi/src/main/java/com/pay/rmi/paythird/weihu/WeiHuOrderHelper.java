package com.pay.rmi.paythird.weihu;

import cn.hutool.json.JSONObject;
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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Component
public class WeiHuOrderHelper extends OrderApiFactory implements ParamsService, ReturnDownService, HttpService {

    public void init(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        this.channel = channel;
        this.mcpConfig = mcpConfig;
        this.reqParams = reqParams;
    }

    @Override
    public Map<String, String> requestToUpParams(OrderReqParams reqParams) {
        Optional<UpPayTypeEntity> upPayTypeEntity = channel.getUpPayTypes().stream()
                .filter(e -> e.getPayType().getPayTypeFlag().equals(reqParams.getOutChannel())).findFirst();
        Assert.mustBeTrue(upPayTypeEntity.isPresent(), "新城不支持的支付方式:" + reqParams.getOutChannel());
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar calendar = Calendar.getInstance();

        String payType = upPayTypeEntity.get().getUpPayTypeFlag();
        Map<String, String> param = new TreeMap<>();
        param.put("order_no", reqParams.getOrderNo());
        param.put("amount", new DecimalFormat("#.00").format(new BigDecimal(reqParams.getAmount())));
        param.put("return_url", reqParams.getReturnUrl());
        param.put("notify_url", getCallbackUrl());
        param.put("currency", "CNY");
        param.put("trade_code", payType);
        param.put("client_ip", reqParams.getReqIp());
        param.put("terminal_type", "wap");
        if(payType.equals("Bank")){
            param.put("bank_method", "cashier");
        }

        params.put("data", JSON.toJSONString(param));
        params.put("service","service.ffff.pay");
        params.put("merchant_id",mcpConfig.getUpMerchantNo());
        params.put("request_time",df.format(calendar.getTime()));
        params.put("nonce_str",System.currentTimeMillis()+"");
        params.put("version","V1.1");
        params.put("sign_type","MD5");
        return params;
    }

    @Override
    public String signToUp(String context, String upKey) {
        //加签
        Map<String, String> paramSign = new TreeMap<>();
        Map maps = (Map)JSON.parse(params.get("data"));
        paramSign.put("order_no", (String) maps.get("order_no"));
        paramSign.put("amount", (String) maps.get("amount"));
        paramSign.put("return_url", (String) maps.get("return_url"));
        paramSign.put("notify_url", (String) maps.get("notify_url"));
        paramSign.put("currency", (String) maps.get("currency"));
        paramSign.put("trade_code", (String) maps.get("trade_code"));
        paramSign.put("client_ip", (String) maps.get("client_ip"));
        paramSign.put("terminal_type", "wap");
        if(maps.get("trade_code").equals("Bank")){
            paramSign.put("bank_method", "cashier");
        }
        paramSign.put("service",params.get("service"));
        paramSign.put("merchant_id",params.get("merchant_id"));

        paramSign.put("request_time",params.get("request_time"));
        paramSign.put("nonce_str",params.get("nonce_str"));
        paramSign.put("version",params.get("version"));
        String signParams = SignUtils.buildParamsIgnoreNull(paramSign) + "&key=" + mcpConfig.getUpKey();
        return PayMD5.MD5Encode(signParams).toUpperCase();
    }

    @Override
    public OrderApiRespParams returnDown(String result) {
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });

        String code = resultMap.get("resp_code");
        org.springframework.util.Assert.isTrue("1000".equals(code), "威虎请求状态响应:" + resultMap.get("msg"));
        String data = resultMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });

        String payUrl = resultData.get("pay_info");

        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams.setCode_url(payUrl);

        return  orderApiRespParams;
    }

    @Override
    public String httpPost(Map<String, String> params) {
        Map<String, String> head = new HashMap();
        head.put("Content-Type", "application/json");
        return HttpKit.post(channel.getUpPayUrl(), JSON.toJSONString(this.params), head);
    }
}
