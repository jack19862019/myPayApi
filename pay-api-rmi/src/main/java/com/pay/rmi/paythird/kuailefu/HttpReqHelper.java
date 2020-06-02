package com.pay.rmi.paythird.kuailefu;

import com.alibaba.fastjson.JSON;
import com.pay.rmi.paythird.kuailefu.util.HttpKit;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HttpReqHelper {


    public String httpRequestToUp(String payUrl, Map<String, String> requestToUpParams) {
        Map<String, String> head = new HashMap();
        head.put("Content-Type", "application/json");
        return HttpKit.post(payUrl, JSON.toJSONString(requestToUpParams), head);
    }
}
