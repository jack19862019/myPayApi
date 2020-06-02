package com.pay.rmi.paythird.kuailefu;

import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
public class SignBuilder {

    public String signToUp(String context, String upKey) {
        return PayMD5.MD5Encode(context + upKey).toLowerCase();
    }

    static String formatSignData(Map<String, String> signDataMap) {
        Set<String> sortedSet = new TreeSet<String>(signDataMap.keySet());
        StringBuffer sb = new StringBuffer();
        for (String key : sortedSet) {
            if ("sign".equalsIgnoreCase(key)) {
                continue;
            }

            if (signDataMap.get(key) != null) {
                String v = String.valueOf(signDataMap.get(key));
                if (StringUtils.isNotBlank(v)) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(v);
                    sb.append("&");
                }
            }
        }
        String s = sb.toString();
        if (s.length() > 0) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
