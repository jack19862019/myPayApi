package com.pay.rmi.paythird.kuailefu;

import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
public class SignHelper {

    public String signToUp(String context, String upKey) {
        return PayMD5.MD5Encode(context + upKey).toLowerCase();
    }
}
