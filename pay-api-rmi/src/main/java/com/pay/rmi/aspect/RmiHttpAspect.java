package com.pay.rmi.aspect;

import com.pay.rmi.paylog.PayLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RmiHttpAspect {

    @Autowired
    private PayLogService payLogService;


    @Pointcut("execution(* com.pay.rmi.paythird.kuailefu.HttpReqHelper.httpRequestToUp(..))")
    public void logHttpReq() {

    }


    @Before("logHttpReq()")
    public void logHttpReq(JoinPoint pjd) {
        System.out.println("*******************************");
    }


    @AfterReturning(value = "logHttpReq()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("The method " + methodName + " return with " + result);
    }
}
