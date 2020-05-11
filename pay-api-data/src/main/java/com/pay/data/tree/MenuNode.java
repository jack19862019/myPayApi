package com.pay.data.tree;

import com.pay.data.entity.SysMenuEntity;
import com.pay.common.enums.MenuLevel;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@BeanCopySource(source = SysMenuEntity.class)
public class MenuNode {

    private Long id;

    @CopyProperty(property = "parent.id")
    private Long pid;

    private Date createTime;

    @CopyProperty(property = "name")
    private String title;

    private int sort;

    private Boolean isDisplay;

    private String path;

    @CopyProperty(property = "alias")
    private String key;

    private String icon;

    private MenuLevel menuLevel;

    private List<MenuNode> children;
}
