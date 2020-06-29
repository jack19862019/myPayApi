package com.pay.gateway.aspect;

import com.pay.common.enums.LogType;
import com.pay.common.utils.BrowserUtils;
import com.pay.common.utils.IPUtils;
import com.pay.common.utils.RequestHolder;
import com.pay.common.utils.ThrowableUtil;
import com.pay.data.entity.SysLogEntity;
import com.pay.manager.pc.log.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static com.pay.common.security.SecurityUtils.getUsername;

@Slf4j
@Aspect
@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SysLogAspect {

    private long currentTime = 0L;

    private final SysLogService sysLogService;

    public SysLogAspect(SysLogService sysLogService) {
        this.sysLogService = sysLogService;
    }

    @Pointcut("@annotation(io.swagger.annotations.ApiOperation)")
    public void logPointCut() {

    }

    /**
     * 环绕日志
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("logPointCut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        currentTime = System.currentTimeMillis();
        result = joinPoint.proceed();
        SysLogEntity log = new SysLogEntity(LogType.INFO, System.currentTimeMillis() - currentTime);
        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        sysLogService.insert(this.getUserName(), BrowserUtils.getBrowser(request), IPUtils.getIpAddr(request), joinPoint, log);
        return result;
    }

    /**
     * 异常日志
     * @param joinPoint
     * @param e
     */
    @AfterThrowing(pointcut = "logPointCut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        SysLogEntity log = new SysLogEntity(LogType.ERROR, System.currentTimeMillis() - currentTime);
        log.setExceptionDetail(ThrowableUtil.getStackTrace(e).getBytes());
        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        sysLogService.insert(this.getUserName(), BrowserUtils.getBrowser(request), IPUtils.getIpAddr(request), (ProceedingJoinPoint) joinPoint, log);
    }

    private String getUserName() {
        try {
            return getUsername();

        }catch (Exception e){
            return "";
        }
    }


}
