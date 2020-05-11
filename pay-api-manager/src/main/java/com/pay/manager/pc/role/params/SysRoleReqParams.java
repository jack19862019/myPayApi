package com.pay.manager.pc.role.params;

import com.pay.common.enums.RoleType;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SysRoleReqParams {

    @NotBlank(message = "角色编号不能为空")
    private String num;

    @NotBlank(message = "角色名称不能为空")
    private String name;

    private String remark;

    @NotNull(message = "角色类型不能为空")
    private RoleType roleType;

    @NotNull(message = "请给角色分配菜单")
    @Valid
    private List<RoleMenusParams> menuNodes;
}
