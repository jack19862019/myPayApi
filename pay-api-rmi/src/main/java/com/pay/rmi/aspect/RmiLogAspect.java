package com.pay.rmi.aspect;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.pay.common.utils.RequestHolder;
import com.pay.rmi.service.ApiPayLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class RmiLogAspect {

    @Autowired
    private ApiPayLogService apiPayLogService;


    @Pointcut("execution(* com.pay.rmi.paythird.*.*.*(..))")
    public void logPointCut() {

    }

    /**
     * 环绕日志
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("logPointCut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        log.info("class 【{}】 method 【{}】 start.",
                joinPoint.getTarget().getClass().getSimpleName(), method);
        Object val = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            val = joinPoint.proceed();
        } catch (Exception e) {
            System.out.println("=================");
        } finally {
            stopwatch.stop();
            int elapsed = (int) stopwatch.elapsed(TimeUnit.MILLISECONDS);
            log.info("class 【{}】 method 【{}】 finished, {}ms, result:{}",
                    joinPoint.getTarget().getClass().getSimpleName(), method, elapsed, JSON.toJSONString(val));
            //退出方法前，清除日志配置
        }
        return val;
    }

    /**
     * 异常日志
     *
     * @param joinPoint
     * @param e
     */
    @AfterThrowing(pointcut = "logPointCut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        System.out.println("***************");
    }
}
