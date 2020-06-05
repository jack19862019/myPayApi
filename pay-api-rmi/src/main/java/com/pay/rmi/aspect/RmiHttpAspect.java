package com.pay.rmi.aspect;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.IsValue;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;


@Slf4j
@Aspect
@Component
public class RmiHttpAspect extends BaseAspect {


    @Pointcut("execution(* com.pay.rmi.paythird.*.*Helper.httpPost(..))")
    public void logHttpReq() {

    }


    @Before("logHttpReq()")
    public void logHttpReq(JoinPoint pjd) {
        map.put(sortStr, 2);
        map.put(methodName, pjd.getSignature().getName());
        map.put(rStr, JSON.toJSONString(pjd.getArgs()[0]));
    }


    @AfterReturning(value = "logHttpReq()", returning = "result")
    public void afterReturning(Object result) {
        map.put(cStr, result);
        map.put(isValue, IsValue.ZC);
        savePayLog();
        map.remove(cStr);
        map.remove(isValue);
        map.remove(rStr);
        map.remove(methodName);
    }

    @AfterThrowing(value = "logHttpReq()", throwing = "ex")
    public void afterThrowing(Exception ex) {
        StackTraceElement stackTraceElement = ex.getStackTrace()[0];
        String className = stackTraceElement.getClassName();
        String lineNumber = String.valueOf(stackTraceElement.getLineNumber());
        map.put(cStr, className + lineNumber + "行: " + ex);
        map.put(isValue, IsValue.BC);
        savePayLog();
    }
}
