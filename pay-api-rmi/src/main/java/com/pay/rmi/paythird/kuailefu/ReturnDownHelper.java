package com.pay.rmi.paythird.kuailefu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mysema.commons.lang.URLEncoder;
import com.pay.rmi.api.resp.OrderApiRespParams;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

@Component
public class ReturnDownHelper {


    public OrderApiRespParams returnDown(String result, OrderApiRespParams orderApiRespParams) {
        Map<String, String> resultMap = JSON.parseObject(result, new TypeReference<Map<String, String>>() {
        });
        String code = resultMap.get("detail");
        Assert.isTrue("0".equals(resultMap.get("code")), "快乐付上游支付状态响应:" + resultMap.get("msg"));
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
        return orderApiRespParams;
    }
}
