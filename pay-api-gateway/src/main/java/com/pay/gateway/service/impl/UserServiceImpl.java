package com.pay.gateway.service.impl;

import com.pay.common.exception.Assert;
import com.pay.data.entity.SysRoleEntity;
import com.pay.data.entity.SysUserEntity;
import com.pay.data.mapper.SysUserRepository;
import com.pay.gateway.auth.resp.Role;
import com.pay.gateway.service.UserService;
import com.pay.gateway.service.dto.UserResp;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserRepository sysUserRepository;


    @Override
    public UserResp findByName(String userName) {
        SysUserEntity user = sysUserRepository.findByUsername(userName);
        Assert.isEmpty("用户不存在", user);
        Assert.isEmpty("用户角色不存在", user.getRole().getName());
        return BeanCopyUtils.copyBean(user, UserResp.class);
    }
}
