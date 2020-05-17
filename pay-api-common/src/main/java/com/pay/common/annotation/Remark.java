package com.pay.common.annotation;


import javax.validation.Constraint;
import javax.validation.constraints.NotBlank;
import java.lang.annotation.*;


@Documented
@Constraint(
        validatedBy = {}
)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Remark {

    // 资源名称，用于描述接口功能
    String name();

}
