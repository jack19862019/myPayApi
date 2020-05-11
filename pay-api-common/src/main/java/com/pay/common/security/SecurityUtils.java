package com.pay.common.security;

import com.pay.common.enums.RoleType;
import com.pay.common.exception.Assert;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public class SecurityUtils {

    public static UserInfo getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.loginFail("登录过期，请重新登录", authentication);
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        return new UserInfo(jwtUser.getId(), jwtUser.getUsername(), jwtUser.getRoleType());
    }

    public static String getUsername() {
        UserInfo userDetails = getUserDetails();
        return userDetails.getUsername();
    }

    public static Long getUserId() {
        UserInfo userDetails = getUserDetails();
        return userDetails.getId();
    }

    public static RoleType getRoleType() {
        UserInfo userDetails = getUserDetails();
        return userDetails.getRoleType();
    }
}
