package com.pay.rmi.aspect;

import com.pay.common.enums.IsValue;
import com.pay.rmi.paylog.PayLogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BaseAspect {

    protected String sortStr = "sortStr";
    protected String methodName = "methodName";
    protected String orderNo = "orderNo";
    protected String channelNo = "channelNo";
    protected String rStr = "rStr";
    protected String cStr = "cStr";
    protected String optionUser = "optionUser";
    protected IsValue isValue = IsValue.ZC;//是否报错


    protected static ConcurrentMap<Object, Object> map = new ConcurrentHashMap<>();

    @Autowired
    private PayLogService payLogService;

    protected void savePayLog() {
        payLogService.insertPayOrderLog(
                IsValue.getStatusByCode(((IsValue)map.get(isValue)).getCode()),
                Integer.valueOf(map.get(sortStr).toString()),
                map.get(methodName).toString(),
                map.get(orderNo).toString(),
                map.get(channelNo).toString(),
                map.get(rStr).toString(),
                map.get(cStr).toString(),
                map.get(optionUser).toString());
    }
}
