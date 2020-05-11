package com.pay.manager.pc.role.params;

import com.pay.data.entity.SysMenuEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyCollection;
import lombok.Data;

import java.util.Set;

@Data
@BeanCopySource(source = SysRoleReqParams.class)
public class RoleBeanOption {

    private String name;

    private String num;

    private String remark;

    @CopyCollection(targetClass = SysMenuEntity.class, property = "menuNodes")
    private Set<SysMenuEntity> menus;
}
