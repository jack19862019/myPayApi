package com.pay.rmi.paythird.katong.util;


import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class KaTongUtil {

    public static String encodeMD5(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String generateSignReduce(Object o) {
        JSONObject parse = JSONObject.parseObject(JSONObject.toJSONString(o));
        String result = parse.keySet()
                .stream().filter(key -> !"sign".equalsIgnoreCase(key))
                .filter(key -> !StringUtils.isEmpty(parse.getString(key)))
                .sorted().map(key -> {
                    try {
                        return key + "=" + URLEncoder.encode(parse.getString(key), StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .reduce("", (a, b) -> a + "&" + b).substring(1);
        return result;
    }


}

