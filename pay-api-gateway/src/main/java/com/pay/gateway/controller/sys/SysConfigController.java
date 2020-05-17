package com.pay.gateway.controller.sys;


import com.pay.common.annotation.SysParamsValidator;
import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.config.SysConfigService;
import com.pay.manager.pc.config.params.SysConfigPrams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "系统-系统配置接口")
@RestController
@RequestMapping("/sys/config")
public class SysConfigController extends AbstractController {



    @PostMapping
    @SysParamsValidator
    @ApiOperation(value = "新增")
    public Result insertConfig(@RequestBody SysConfigPrams sysConfigPrams) {
        sysConfigService.insertConfig(sysConfigPrams);
        return Result.success();
    }

    @PutMapping("{id}")
    @SysParamsValidator
    @ApiOperation(value = "修改")
    public Result updateConfig(@PathVariable Long id, @RequestBody SysConfigPrams sysConfigPrams) {
        sysConfigService.updateConfig(id, sysConfigPrams);
        return Result.success();
    }

    @ApiOperation(value = "删除")
    @DeleteMapping("{id}")
    public Result deleteConfig(@PathVariable Long id) {
        sysConfigService.deleteConfig(id);
        return Result.success();
    }

    @GetMapping
    @ApiOperation(value = "列表")
    public Result getConfig() {
        return Result.success(sysConfigService.getConfig());
    }

    @Autowired
    SysConfigService sysConfigService;

}
