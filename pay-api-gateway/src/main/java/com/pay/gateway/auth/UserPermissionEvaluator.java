package com.pay.gateway.auth;

import com.pay.common.security.JwtUser;
import com.pay.data.entity.SysMenuEntity;
import com.pay.data.mapper.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
@Component
public class UserPermissionEvaluator implements PermissionEvaluator {
    @Autowired
    private SysUserRepository sysUserRepository;
    @Override
    public boolean hasPermission(Authentication authentication, Object targetUrl, Object permission) {
        // 获取用户信息
        JwtUser jwtUser =(JwtUser) authentication.getPrincipal();
        // 查询用户权限(这里可以将权限放入缓存中提升效率)
        Set<String> permissions = new HashSet<>();
        Set<SysMenuEntity> menus = sysUserRepository.findByUsername(jwtUser.getUsername()).getRole().getMenus();
        for (SysMenuEntity sysMenuEntity:menus) {
            permissions.add(sysMenuEntity.getPath());
        }
        // 权限对比
        if (permissions.contains(permission.toString())){
            return true;
        }
        return false;
    }
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
