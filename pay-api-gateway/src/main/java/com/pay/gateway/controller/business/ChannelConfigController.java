package com.pay.gateway.controller.business;


import com.pay.common.utils.Result;
import com.pay.data.params.OrderReqParams;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.channel.ChannelConfigService;
import com.pay.manager.pc.channel.params.ChannelConfigIndexParams;
import com.pay.manager.pc.channel.params.ChannelConfigReqParams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "系统-通道(各项)配置接口")
@RestController
@RequestMapping("/channel/config")
public class ChannelConfigController extends AbstractController {


    @Autowired
    ChannelConfigService channelConfigService;


    @GetMapping
    @ApiOperation(value = "通道入参配置初始化")
    public Result initChannelConfig() {
        return Result.success(channelConfigService.getThisReqParams());
    }

    @PostMapping
    @ApiOperation(value = "通道入参配置新增")
    public Result saveChannelConfig(@RequestBody ChannelConfigReqParams reqParams) {
        channelConfigService.insertChannelConfig(reqParams);
        return Result.success();
    }

    @GetMapping(value = "/{id}")
    @ApiOperation(value = "通道入参配置查询")
    public Result getChannelConfig(@PathVariable Long id) {
        return Result.success(channelConfigService.getChannelConfig(id));
    }

    @PutMapping
    @ApiOperation(value = "通道入参配置修改")
    public Result updateChannelConfig(@RequestBody ChannelConfigReqParams reqParams) {
        channelConfigService.updateChannelConfig(reqParams);
        return Result.success();
    }


}
