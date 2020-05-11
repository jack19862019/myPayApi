package com.pay.manager.pc.role.params;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RoleMenusParams {

    @NotNull(message = "菜单编号不能为空")
    private Long id;

    @NotBlank(message = "菜单名称不能为空")
    private String name;
}
