package com.pay.rmi.aspect;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.IsValue;
import com.pay.rmi.paylog.PayLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Aspect
@Component
public class RmiReturnAspect extends BaseAspect {


    @Pointcut("execution(* com.pay.rmi.paythird.*.*Helper.returnDown(..))")
    public void logReturnDown() {

    }


    @Before("logReturnDown()")
    public void logHttpReq(JoinPoint pjd) {
        map.put(sortStr, 3);
        map.put(methodName, pjd.getSignature().getName());
        map.put(rStr, pjd.getArgs()[0]);
    }


    @AfterReturning(value = "logReturnDown()", returning = "result")
    public void afterReturning(Object result) {
        map.put(cStr, JSON.toJSONString(result));
        map.put(isValue, IsValue.ZC);
        savePayLog();
        map.remove(cStr);
        map.remove(isValue);
        map.remove(rStr);
        map.remove(methodName);
    }

    @AfterThrowing(value = "logReturnDown()", throwing = "ex")
    public void afterThrowing(Exception ex) {
        StackTraceElement stackTraceElement = ex.getStackTrace()[0];
        String className = stackTraceElement.getClassName();
        String lineNumber = String.valueOf(stackTraceElement.getLineNumber());
        map.put(cStr, className + lineNumber + "行: " + ex);
        map.put(isValue, IsValue.BC);
        savePayLog();
    }
}
