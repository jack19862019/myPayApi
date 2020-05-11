package com.pay.rmi.common.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.Set;

public class HttpsParams {

    public static HttpEntity<Object> buildFormEntity(Map<String, String> params) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            form.add(key, params.get(key));
        }

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=utf-8");
        return new HttpEntity<>(form, headers);
    }

    public static HttpEntity<Object> buildFormEntityXml(Map<String, String> params) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", MediaType.APPLICATION_XML + ";charset=utf-8");
        String xmlString = MapToXml.toString(params);
        return new HttpEntity<>(xmlString, headers);
    }
}
