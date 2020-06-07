package com.pay.rmi.aspect;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.IsOrder;
import com.pay.common.enums.IsValue;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class RmiCallBackAspect extends BaseAspect {


    @Pointcut("execution(* com.pay.rmi.paythird.*.*Helper.init(..))")
    public void logCallBack() {

    }


    @Before("logCallBack()")
    public void logSignToUp(JoinPoint pjd) {
        map.put(isOrder, IsOrder.BACK);
        map.put(sortStr, 0);
        map.put(methodName, pjd.getSignature().getName());
        Map<String, String> mapReqParams = (Map<String, String>) pjd.getArgs()[2];
        map.put(orderNo, ((OrderEntity) pjd.getArgs()[1]).getOrderNo());
        map.put(optionUser, ((McpConfigEntity) pjd.getArgs()[0]).getChannel().getChannelFlag() + ":" + callbackOptionUser);
        map.put(rStr, JSON.toJSONString(mapReqParams));
        map.put(channelNo, ((McpConfigEntity) pjd.getArgs()[0]).getChannel().getChannelFlag());
    }

    @AfterReturning(value = "logCallBack()", returning = "result")
    public void afterReturning(Object result) throws NoSuchFieldException, IllegalAccessException {
        Class<?> aClass = result.getClass();
        Field declaredFields = aClass.getDeclaredField("flagSuccess");
        declaredFields.setAccessible(true);
        String status = declaredFields.get(result).toString();

        map.put(cStr, status);
        map.put(isValue, IsValue.ZC);
        savePayLog();
        map.remove(cStr);
        map.remove(isValue);
        map.remove(rStr);
        map.remove(methodName);
    }

    @AfterThrowing(value = "logCallBack()", throwing = "ex")
    public void afterThrowing(Exception ex) {
        StackTraceElement stackTraceElement = ex.getStackTrace()[0];
        String className = stackTraceElement.getClassName();
        String lineNumber = String.valueOf(stackTraceElement.getLineNumber());
        map.put(cStr, className + lineNumber + "行: " + ex);
        map.put(isValue, IsValue.BC);
        savePayLog();
    }
}
