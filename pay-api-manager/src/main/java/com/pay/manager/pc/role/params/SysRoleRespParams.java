package com.pay.manager.pc.role.params;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.pay.common.enums.RoleType;
import com.pay.data.entity.SysRoleEntity;
import com.pay.data.tree.MenuNode;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@BeanCopySource(source = SysRoleEntity.class)
public class SysRoleRespParams implements Serializable {

    private Long id;

    private String name;

    private RoleType roleType;

    private String num;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Set<MenuNode> menuNodes;

}
