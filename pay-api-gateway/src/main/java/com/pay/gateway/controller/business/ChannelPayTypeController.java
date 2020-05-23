package com.pay.gateway.controller.business;

import com.pay.common.annotation.SysParamsValidator;
import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.channel.ChannelPayTypeService;
import com.pay.manager.pc.channel.params.ChannelPayTypeParams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Api(tags = "业务-通道支付方式管理接口")
@RequestMapping("/sys/channel/pay/type")
public class ChannelPayTypeController extends AbstractController {

    @Autowired
    ChannelPayTypeService channelPayTypeService;

    @PostMapping("/{channelId}")
    @SysParamsValidator
    @ApiOperation(value = "新增通道支付方式")
    public Result addChannelType(@PathVariable Long channelId, @RequestBody List<ChannelPayTypeParams> reqParamsList) {
        channelPayTypeService.insert(channelId, reqParamsList);
        return Result.success();
    }

    @PutMapping("/{channelId}")
    @SysParamsValidator
    @ApiOperation(value = "修改通道支付方式")
    public Result updateChannelType(@PathVariable Long channelId, @RequestBody List<ChannelPayTypeParams> reqParamsList) {
        channelPayTypeService.update(channelId, reqParamsList);
        return Result.success();
    }


}
