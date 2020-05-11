package com.pay.manager.pc.menu.params;


import com.pay.data.entity.SysMenuEntity;
import com.pay.common.enums.MenuLevel;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@BeanCopySource(source = SysMenuEntity.class)
public class SysMenuResParams implements Serializable {

    @CopyProperty(property = "parent.id")
    private Long pid;

    @CopyProperty(property = "parent.name")
    private String pidName;

    private Long id;

    private Date createTime;

    private String name;

    private int sort;

    private String path;

    private String alias;

    private String icon;

    private MenuLevel menuLevel;

}
