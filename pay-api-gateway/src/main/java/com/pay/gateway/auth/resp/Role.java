package com.pay.gateway.auth.resp;

import com.pay.common.enums.RoleType;
import com.pay.data.entity.SysRoleEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

@Data
@BeanCopySource(source = SysRoleEntity.class)
public class Role {

    private Long id;

    private String name;

    private String num;

    private RoleType roleType;
}
