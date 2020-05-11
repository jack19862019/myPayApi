package com.pay.gateway.controller.business;

import com.pay.common.annotation.SysParamsValidator;
import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.channel.ChannelContactService;
import com.pay.manager.pc.contact.ContactParams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Api(tags = "业务-通道联系方式接口")
@RequestMapping("/sys/channel/contact/")
public class ChannelContactController extends AbstractController {

    @PutMapping("{channelId}")
    @SysParamsValidator
    @ApiOperation(value = "修改通道联系方式")
    public Result updateChannelContact(@PathVariable Long channelId, @RequestBody List<ContactParams> contactParams) {
        channelContactService.insertOrUpdate(channelId, contactParams);
        return Result.success();
    }


    @GetMapping("{channelId}/list")
    @ApiOperation(value = "通道联系方式分页")
    public Result getChannelContactList(@PathVariable Long channelId) {
        return Result.success(channelContactService.getChannelContactList(channelId));
    }


    @Autowired
    ChannelContactService channelContactService;
}
