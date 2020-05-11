package com.pay.gateway.controller.sys;


import com.pay.common.annotation.SysParamsValidator;
import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.role.params.RoleQuery;
import com.pay.manager.pc.role.params.SysRoleReqParams;
import com.pay.manager.pc.role.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "系统-角色管理接口")
@RestController
@RequestMapping("/sys/role/")
public class SysRoleController extends AbstractController {

    @GetMapping("page")
    @ApiOperation(value = "角色列表")
    public Result getRoleList(RoleQuery roleQuery) {
        return Result.success(sysRoleService.selectList(roleQuery));
    }

    @PostMapping
    @SysParamsValidator
    @ApiOperation(value = "新增角色")
    public Result addRole(@RequestBody SysRoleReqParams reqParams) {
        sysRoleService.insert(reqParams);
        return Result.success();
    }

    @ApiOperation(value = "修改角色")
    @PutMapping("{id}")
    @SysParamsValidator
    public Result updateRole(@RequestBody SysRoleReqParams reqParams, @PathVariable Long id) {
        sysRoleService.update(id, reqParams);
        return Result.success();
    }

    @ApiOperation(value = "删除角色")
    @DeleteMapping("{id}")
    public Result deleteRole(@PathVariable Long id) {
        sysRoleService.delete(id);
        return Result.success();
    }

    @ApiOperation(value = "角色详情")
    @GetMapping("{id}")
    public Result getRole(@PathVariable Long id) {
        return Result.success(sysRoleService.select(id));
    }


    @Autowired
    private SysRoleService sysRoleService;
}
