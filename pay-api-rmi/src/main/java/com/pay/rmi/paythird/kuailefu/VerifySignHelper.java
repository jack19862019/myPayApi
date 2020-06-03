package com.pay.rmi.paythird.kuailefu;

import com.pay.common.exception.Assert;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

@Component
public class VerifySignHelper {

    public void verifySign(Map<String, String> params, String upKey) {
        Map<String, String> treeMap = new TreeMap<>(params);
        String sign = treeMap.remove("sign");
        String signStr = StrKit.formatSignData(params);
        String newSign = PayMD5.MD5Encode(signStr + upKey).toLowerCase();
        Assert.mustBeTrue(newSign.equals(sign), "验签失败！");
    }

}
