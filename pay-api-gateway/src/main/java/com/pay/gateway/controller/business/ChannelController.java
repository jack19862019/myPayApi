package com.pay.gateway.controller.business;

import com.pay.common.annotation.SysParamsValidator;
import com.pay.common.page.PageReqParams;
import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.channel.ChannelService;
import com.pay.manager.pc.channel.params.ChannelQuery;
import com.pay.manager.pc.channel.params.ChannelReqParams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@Api(tags = "业务-通道管理接口")
@RequestMapping("/sys/channel/")
public class ChannelController extends AbstractController {

    @PostMapping
    @SysParamsValidator
    @ApiOperation(value = "新增通道")
    public Result addChannel(@RequestBody ChannelReqParams reqParams) {
        return Result.success(channelService.insert(reqParams));
    }

    @PutMapping("{id}")
    @SysParamsValidator
    @ApiOperation(value = "修改通道")
    public Result updateChannel(@RequestBody ChannelReqParams reqParams, @PathVariable Long id) {
        channelService.update(reqParams, id);
        return Result.success();
    }

    @DeleteMapping("{id}")
    @ApiOperation(value = "删除通道")
    public Result deleteChannel(@PathVariable Long id) {
        channelService.delete(id);
        return Result.success();
    }

    @GetMapping("{id}")
    @ApiOperation(value = "通道详情")
    public Result getChannel(@PathVariable Long id) {
        return Result.success(channelService.select(id));
    }

    @GetMapping("page")
    @ApiOperation(value = "通道分页")
    public Result getChannelPage(ChannelQuery query, PageReqParams reqParams) {
        return Result.success(channelService.selectPage(query, reqParams));
    }


    @Autowired
    ChannelService channelService;
}
