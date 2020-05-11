package com.pay.common.utils;

import com.pay.common.annotation.EnumValidAnnotation;
import com.pay.common.exception.CustomerException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnumValid implements ConstraintValidator<EnumValidAnnotation, String> {

    private Class<?>[] cls;

    @Override
    public void initialize(EnumValidAnnotation constraintAnnotation) {
        cls = constraintAnnotation.target();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (cls.length > 0) {
            for (Class<?> cl : cls
            ) {
                try {
                    if (cl.isEnum()) {
                        //枚举类验证
                        Object[] objs = cl.getEnumConstants();
                        Method method = cl.getMethod("name");
                        for (Object obj : objs
                        ) {
                            Object code = method.invoke(obj, null);
                            if (value.equals(code.toString())) {
                                return true;
                            }
                        }
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new CustomerException("枚举校验异常");
                }
            }
        } else {
            return true;
        }
        return false;
    }
}
