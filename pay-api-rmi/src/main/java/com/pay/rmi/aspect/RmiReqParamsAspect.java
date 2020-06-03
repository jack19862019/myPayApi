package com.pay.rmi.aspect;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.IsValue;
import com.pay.data.params.OrderReqParams;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RmiReqParamsAspect extends BaseAspect {


    @Pointcut("execution(* com.pay.rmi.paythird.*.*.requestToUpParams(..))")
    public void logRequestToUpParams() {

    }


    @Before("logRequestToUpParams()")
    public void logRequestToUpParams(JoinPoint pjd) {
        OrderReqParams orderReqParams = (OrderReqParams) pjd.getArgs()[2];
        map.put(orderNo, orderReqParams.getOrderNo());
        map.put(optionUser, orderReqParams.getUserId());
        map.put(rStr, JSON.toJSONString(orderReqParams));
        map.put(channelNo, orderReqParams.getChannelNo());
        map.put(methodName, pjd.getSignature().getName());
    }

    @AfterReturning(value = "logRequestToUpParams()", returning = "result")
    public void afterReturning(Object result) {
        map.put(cStr, JSON.toJSONString(result));
        map.put(isValue, IsValue.ZC);
        savePayLog();
    }

    @AfterThrowing(value = "logRequestToUpParams()", throwing = "ex")
    public void afterThrowing(Exception ex) {
        StackTraceElement stackTraceElement= ex.getStackTrace()[0];
        String className = stackTraceElement.getClassName();
        String lineNumber = String.valueOf(stackTraceElement.getLineNumber());
        map.put(cStr, className + lineNumber + "行: " + ex);
        map.put(isValue, IsValue.BC);
        savePayLog();
    }
}
