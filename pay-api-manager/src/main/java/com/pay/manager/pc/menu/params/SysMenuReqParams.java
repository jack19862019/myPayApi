package com.pay.manager.pc.menu.params;

import com.pay.common.enums.MenuLevel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SysMenuReqParams {

    @NotBlank(message = "菜单名称不能为空")
    private String name;

    private Integer sort = 0;

    @NotBlank(message = "请定义菜单的路径")
    private String path;

    @NotBlank(message = "菜单标识不能为空")
    private String alias;

    private String icon;

    @NotNull(message = "菜单类型不能为空")
    private MenuLevel menuLevel;

    private Long pid;
}
