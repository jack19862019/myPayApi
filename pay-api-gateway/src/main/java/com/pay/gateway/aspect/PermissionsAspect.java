package com.pay.gateway.aspect;

import com.pay.data.mapper.SysUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class PermissionsAspect {

    private final SysUserRepository sysUserRepository;

    public PermissionsAspect(SysUserRepository sysUserRepository) {
        this.sysUserRepository = sysUserRepository;
    }

    /*@Pointcut(value = "execution(com.pay.gateway.controller..*())")
    public void PermissionsPointCut() {

    }*/


}
