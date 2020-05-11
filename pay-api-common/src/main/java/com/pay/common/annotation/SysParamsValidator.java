package com.pay.common.annotation;

import java.lang.annotation.*;

/**
 * 系统参数校验注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysParamsValidator {

}
