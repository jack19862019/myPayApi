package com.pay.manager.pc.role.params;

import com.pay.data.entity.SysRoleEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

@Data
@BeanCopySource(source = SysRoleEntity.class)
public class SysUserRolesResParams {

    private Long id;

    private String name;
}
