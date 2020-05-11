package com.pay.common.aop;

import com.pay.common.validator.ValidationUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Created by 刘越群 on 2019/3/11.
 * RequestParamsCheck注解入参校验切面处理类
 */
@Aspect
@Component
public class HibernateParamsAspect {


    @Pointcut("@annotation(com.pay.common.annotation.SysParamsValidator)")
    public void cutRequestParams() {

    }

    /**
     * 执行方法前统一执行参数校验
     */
    @Before("cutRequestParams()")
    public void checkRequestParams(JoinPoint point) {
        Object[] args = point.getArgs();
        for (Object object : args) {
            ValidationUtils.validate(object);
        }
    }

}
