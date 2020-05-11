package com.pay.gateway.controller.sys;

import com.pay.common.annotation.SysParamsValidator;
import com.pay.common.page.PageReqParams;
import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.user.SysUserService;
import com.pay.manager.pc.user.params.SysUserAddReqParams;
import com.pay.manager.pc.user.params.SysUserPasswordParams;
import com.pay.manager.pc.user.params.SysUserUpdateReqParams;
import com.pay.manager.pc.user.params.UserQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@Api(tags = "系统-用户管理接口")
@RequestMapping("/sys/user/")
public class SysUserController extends AbstractController {

    @GetMapping(value = "menus")
    @ApiOperation(value = "用户菜单")
    public Result getUserMenus() {
        return Result.success(sysUserService.getUserMenus());
    }

    @PostMapping
    @SysParamsValidator
    @ApiOperation(value = "新增用户")
    //@PreAuthorize("hasPermission('/userAdd','sys:user:userAdd')")
    public Result addUser(@RequestBody SysUserAddReqParams reqParams) {
        sysUserService.insert(reqParams);
        return Result.success();
    }

    @ApiOperation(value = "删除用户")
    @DeleteMapping("{id}")
    public Result deleteUser(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.success();
    }

    @SysParamsValidator
    @PutMapping("{id}")
    @ApiOperation(value = "修改用户")
    public Result updateUser(@RequestBody SysUserUpdateReqParams reqParams, @PathVariable Long id) {
        sysUserService.update(id, reqParams);
        return Result.success();
    }

    @GetMapping
    @ApiOperation(value = "用户信息")
    public Result userDetail(Long id) {
        return Result.success(sysUserService.select(id == null ? getUserId() : id));
    }

    @GetMapping("list")
    @ApiOperation(value = "用户列表")
    //@PreAuthorize("hasPermission('/list','sys:user')")
    public Result getUsers(UserQuery userQuery, PageReqParams reqParams) {
        return Result.success(sysUserService.getUsers(userQuery, reqParams));
    }

    @PutMapping("update/password")
    @SysParamsValidator
    @ApiOperation(value = "用户修改密码")
    public Result updatePassword(@RequestBody SysUserPasswordParams passwordParams) {
        sysUserService.updatePassword(passwordParams);
        return Result.success();
    }

    @Autowired
    SysUserService sysUserService;
}
