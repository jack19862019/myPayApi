package com.pay.rmi.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RmiSignAspect extends BaseAspect {


    @Pointcut("execution(* com.pay.rmi.paythird.kuailefu.SignBuilder.signToUp(..))")
    public void logSignToUp() {

    }


    @Before("logSignToUp()")
    public void logSignToUp(JoinPoint pjd) {
        String string = map.get(orderNo).toString();
        System.out.println("******************************************" + orderNo);
    }

    @AfterReturning(value = "logSignToUp()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("The method " + methodName + " return with " + result);
    }

}
