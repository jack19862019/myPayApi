package com.pay.gateway.controller.business;

import com.pay.common.page.PageReqParams;
import com.pay.common.utils.Result;
import com.pay.manager.pc.order.OrderService;
import com.pay.manager.pc.order.params.OrderPageReqParams;
import com.pay.manager.pc.order.params.OrderQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "业务-订单管理接口")
@RequestMapping("/sys/order/")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("page")
    @ApiOperation(value = "订单分页")
    public Result getOrderPage(OrderQuery orderQuery, @Validated PageReqParams reqParams) {
        return Result.success(orderService.selectOrderPage(orderQuery, reqParams));
    }

    @PostMapping("send/callback")
    @ApiOperation(value = "手动回调订单")
    public Result sendCallback(@RequestBody OrderPageReqParams reqParams) {
        orderService.sendCallback(reqParams);
        return Result.success();
    }
}
