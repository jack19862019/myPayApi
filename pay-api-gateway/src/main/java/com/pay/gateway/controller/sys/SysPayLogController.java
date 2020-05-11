package com.pay.gateway.controller.sys;

import com.pay.common.page.PageReqParams;
import com.pay.common.utils.Result;
import com.pay.manager.pc.channel.ChannelService;
import com.pay.manager.pc.channel.params.ChannelQuery;
import com.pay.manager.pc.order.OrderService;
import com.pay.manager.pc.paylog.PayLogQuery;
import com.pay.manager.pc.paylog.PayLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "系统-支付日志接口")
@RequestMapping("/sys/payLog/")
public class SysPayLogController {
    @Autowired
    private PayLogService payLogService;

    @GetMapping("page")
    @ApiOperation(value = "日志分页")
    public Result getOrderPage(PayLogQuery payLogQuery, @Validated PageReqParams reqParams) {
        return Result.success(payLogService.selectByFlagAndPayLogNo(payLogQuery, reqParams));
    }

    @DeleteMapping("{id}")
    @ApiOperation(value = "删除支付日志")
    public Result deletePayType(@PathVariable Long id) {
        payLogService.delete(id);
        return Result.success();
    }

    @GetMapping("{id}")
    @ApiOperation(value = "支付日志详情")
    public Result getPayType(@PathVariable Long id) {
        return Result.success(payLogService.select(id));
    }


    @GetMapping("Leaderboard")
    @ApiOperation(value = "排行榜")
    public Result getCs(int page) {
        return Result.success(orderService.getOrderAmountByChannelFlag(page));
    }

    @Autowired
    ChannelService channelService;
    @Autowired
    OrderService orderService;
}
