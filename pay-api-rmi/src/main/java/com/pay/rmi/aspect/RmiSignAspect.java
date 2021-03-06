package com.pay.rmi.aspect;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.IsOrder;
import com.pay.common.enums.IsValue;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class RmiSignAspect extends BaseAspect {


    @Pointcut("execution(* com.pay.rmi.paythird.*.*Helper.signToUp(..))")
    public void logSignToUp() {

    }


    @Before("logSignToUp()")
    public void logSignToUp(JoinPoint pjd) {
        map.put(isOrder, IsOrder.ORDER);
        map.put(sortStr, 1);
        map.put(methodName, pjd.getSignature().getName());
        String argContext = pjd.getArgs()[0].toString();
        String argUpKey = pjd.getArgs()[1].toString();
        Map mapStr = new HashMap();
        mapStr.put("context", argContext);
        mapStr.put("upKey", argUpKey);
        map.put(rStr, JSON.toJSONString(mapStr));
    }

    @AfterReturning(value = "logSignToUp()", returning = "result")
    public void afterReturning(Object result) {
        map.put(cStr, result);
        map.put(isValue, IsValue.ZC);
        savePayLog();
        map.remove(cStr);
        map.remove(isValue);
        map.remove(rStr);
        map.remove(methodName);
    }

    @AfterThrowing(value = "logSignToUp()", throwing = "ex")
    public void afterThrowing(Exception ex) {
        StackTraceElement stackTraceElement = ex.getStackTrace()[0];
        String className = stackTraceElement.getClassName();
        String lineNumber = String.valueOf(stackTraceElement.getLineNumber());
        map.put(cStr, className + lineNumber + "行: " + ex);
        map.put(isValue, IsValue.BC);
        savePayLog();
    }
}
