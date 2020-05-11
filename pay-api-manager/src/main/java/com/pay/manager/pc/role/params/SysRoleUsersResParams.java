package com.pay.manager.pc.role.params;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.pay.data.entity.SysUserEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyCollection;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@BeanCopySource(source = SysUserEntity.class)
public class SysRoleUsersResParams implements Serializable {

    private String username;

    private String password;

    private String email;

    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @CopyCollection(targetClass = SysUserRolesResParams.class)
    private Set<SysUserRolesResParams> roles;

}
