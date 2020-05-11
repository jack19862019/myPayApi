package com.pay.gateway.controller;


import com.pay.common.security.SecurityUtils;
import com.pay.common.security.UserInfo;

/**
 * Controller公共组件
 * 用户获取当前登录用户
 */
public abstract class AbstractController {

    UserInfo getUser() {
        return SecurityUtils.getUserDetails();
    }

    protected Long getUserId() {
        return SecurityUtils.getUserId();
    }

    protected String getUsername() {
        return SecurityUtils.getUsername();
    }
}
