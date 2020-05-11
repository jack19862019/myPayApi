package com.pay.rmi.aspect;

import com.pay.common.enums.LogType;
import com.pay.common.utils.BrowserUtils;
import com.pay.common.utils.IPUtils;
import com.pay.common.utils.RequestHolder;
import com.pay.data.entity.SysLogEntity;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class RmiLogAspect {

    /*private long currentTime = 0L;

    *//*private final SysLogService sysLogService;

    public RmiLogAspect(SysLogService sysLogService) {
        this.sysLogService = sysLogService;
    }*//*

    @Pointcut("execution(* com.pay.rmi.paythird.*.*.*(..))")
    public void logPointCut() {

    }

    *//**
     * 环绕日志
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     *//*
    @Around("logPointCut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        currentTime = System.currentTimeMillis();
        result = joinPoint.proceed();
        //SysLogEntity log = new SysLogEntity(LogType.INFO, System.currentTimeMillis() - currentTime);
        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        //sysLogService.insert(this.getUserName(), BrowserUtils.getBrowser(request), IPUtils.getIpAddr(request), joinPoint, log);
        return result;
    }

    *//**
     * 异常日志
     *
     * @param joinPoint
     * @param e
     *//*
    @AfterThrowing(pointcut = "logPointCut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        System.out.println("=========");
    }*/
}
