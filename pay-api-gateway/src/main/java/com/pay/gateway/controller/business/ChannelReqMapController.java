package com.pay.gateway.controller.business;


import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.channel.ChannelReqMapCgService;
import com.pay.manager.pc.channel.params.ChannelReqMapReqParams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "系统-通道(各项)配置接口")
@RestController
@RequestMapping("/channel/req")
public class ChannelReqMapController extends AbstractController {


    @Autowired
    ChannelReqMapCgService channelReqMapCgService;


    @GetMapping
    @ApiOperation(value = "通道入参配置初始化")
    public Result initChannelConfig() {
        return Result.success(channelReqMapCgService.getThisReqParams());
    }

    @PutMapping("/{channelId}")
    @ApiOperation(value = "通道入参配置修改")
    public Result saveChannelConfig(@PathVariable Long channelId, @RequestBody ChannelReqMapReqParams reqParams) {
        channelReqMapCgService.saveChannelConfig(channelId, reqParams);
        return Result.success();
    }

    @GetMapping(value = "/{id}")
    @ApiOperation(value = "通道入参配置查询")
    public Result getChannelConfig(@PathVariable Long id) {
        return Result.success(channelReqMapCgService.getChannelConfig(id));
    }


}
