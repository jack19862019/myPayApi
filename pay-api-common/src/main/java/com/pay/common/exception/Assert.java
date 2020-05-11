package com.pay.common.exception;


import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据校验
 */
public class Assert {

    public static void isEmpty(String errorMsg, Object objects) {
        if (objects == null) {
            throw new CustomerException(errorMsg);
        }
        if (StringUtils.isEmpty(objects.toString())) {
            String simpleName = objects.getClass().getSimpleName();
            throw new CustomerException(simpleName + errorMsg);
        }
    }

    public static void loginFail(String errorMsg, Object objects) {
        if (objects == null) {
            throw new CustomerException(errorMsg, -88);
        }
        if (StringUtils.isEmpty(objects.toString())) {
            String simpleName = objects.getClass().getSimpleName();
            throw new CustomerException(simpleName + errorMsg);
        }
    }


    public static void isEmpty(Object... objects) {
        List<String> strArr = new ArrayList<>();
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] == null) {
                String className = objects[i].getClass().getName();
                strArr.add(className + "对象不能为空");
            }
            if (objects[i] instanceof String) {
                String simpleName = objects[i].getClass().getSimpleName();
                if (StringUtils.isEmpty(objects[i].toString())) {
                    strArr.add(simpleName + "字符串不能为空");
                }
            }
        }
        if (!CollectionUtils.isEmpty(strArr)) {
            StringBuilder stringBuffer = new StringBuilder();
            for (String s : strArr) {
                stringBuffer.append(s).append(",");
            }
            throw new CustomerException(stringBuffer.toString().substring(stringBuffer.length() - 1));
        }
    }


    public static void mustBeTrue(boolean condition, String message) {
        if (!condition) {
            throw new CustomerException(message, 500);
        }

    }

}
