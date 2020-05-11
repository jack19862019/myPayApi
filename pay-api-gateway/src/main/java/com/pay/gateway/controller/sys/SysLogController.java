package com.pay.gateway.controller.sys;

import com.pay.common.page.PageReqParams;
import com.pay.common.utils.Result;
import com.pay.manager.pc.log.LogQuery;
import com.pay.manager.pc.log.SysLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Api(tags = "系统-系统日志接口")
@RestController
@RequestMapping("/sys/log/")
public class SysLogController {

    @ApiOperation(value = "删除日志")
    @DeleteMapping("{id}")
    public Result deleteLog(@PathVariable Long id) {
        sysLogService.delete(id);
        return Result.success();
    }

    @GetMapping("{id}")
    @ApiOperation(value = "日志信息")
    public Result logDetail(@PathVariable Long id) { return Result.success(sysLogService.select(id));}

    @GetMapping("page")
    @ApiOperation(value = "日志列表")
    public Result getLogs(LogQuery userQuery,@Validated PageReqParams reqParams) {
        return Result.success(sysLogService.getLogPage(userQuery, reqParams));
    }

    @Autowired
    SysLogService sysLogService;
}
