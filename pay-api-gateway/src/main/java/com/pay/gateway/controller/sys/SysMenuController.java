package com.pay.gateway.controller.sys;

import com.pay.common.annotation.SysParamsValidator;
import com.pay.common.enums.MenuLevel;
import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.menu.params.MenuQuery;
import com.pay.manager.pc.menu.params.SysMenuReqParams;
import com.pay.manager.pc.menu.SysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/sys/menu/")
@Api(tags = "系统-菜单管理接口")
public class SysMenuController extends AbstractController {

    @GetMapping("parent")
    @ApiOperation(value = "根据菜单类型输出上级")
    public Result getParentMenu(MenuLevel menuLevel) {
        return Result.success(sysMenuService.selectParentMenu(menuLevel));
    }

    @PostMapping
    @SysParamsValidator
    @ApiOperation(value = "新增菜单")
    public Result addMenu(@RequestBody SysMenuReqParams reqParams) {
        sysMenuService.insert(reqParams);
        return Result.success();
    }

    @ApiOperation(value = "修改菜单")
    @PutMapping(value = "{id}")
    @SysParamsValidator
    public Result updateMenu(@PathVariable Long id, @RequestBody SysMenuReqParams reqParams) {
        sysMenuService.update(id, reqParams);
        return Result.success();
    }

    @ApiOperation(value = "删除菜单")
    @DeleteMapping(value = "{id}")
    public Result deleteMenu(@PathVariable Long id) {
        sysMenuService.delete(id);
        return Result.success();
    }

    @ApiOperation(value = "菜单列表")
    @GetMapping(value = "list")
    public Result getMenuList(MenuQuery menuQuery) {
        return Result.success(sysMenuService.selectPage(menuQuery));
    }

    @ApiOperation(value = "菜单详情")
    @GetMapping(value = "{id}")
    public Result getMenuList(@PathVariable Long id) {
        return Result.success(sysMenuService.select(id));
    }

    @Autowired
    SysMenuService sysMenuService;

}
