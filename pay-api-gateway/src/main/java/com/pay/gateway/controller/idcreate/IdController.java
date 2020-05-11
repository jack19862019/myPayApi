package com.pay.gateway.controller.idcreate;

import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.cache.CacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Api(tags = "业务-编号自动生成接口")
@RequestMapping("/sys/id/create")
public class IdController extends AbstractController {

    @GetMapping
    @ApiOperation(value = "ID生成器")
    public Result IdCreate(String str) {
        return Result.success(cacheService.numCreate(str));
    }

    @Autowired
    CacheService cacheService;
}
