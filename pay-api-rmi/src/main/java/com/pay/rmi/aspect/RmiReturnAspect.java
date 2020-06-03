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

    @Autowired
    private PayLogService payLogService;


    @Pointcut("execution(* com.pay.rmi.paythird.*.HttpReqHelper.returnDown(..))")
    public void logReturnDown() {

    }


    @Before("logReturnDown()")
    public void logHttpReq(JoinPoint pjd) {
        map.put(rStr, pjd.getArgs()[1]);
        map.put(methodName, pjd.getSignature().getName());
    }


    @AfterReturning(value = "logReturnDown()", returning = "result")
    public void afterReturning(Object result) {
        map.put(cStr, JSON.toJSONString(result));
        map.put(isValue, IsValue.ZC);
        savePayLog();
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
