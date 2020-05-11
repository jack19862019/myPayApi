package com.pay.gateway.controller.indexDemo;

import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.order.OrderService;
import com.pay.manager.pc.statistics.StatisticsBusinessHelper;
import com.pay.manager.pc.statistics.params.DataRespParams;
import com.pay.manager.pc.statistics.params.IndexReqParams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Api(tags = "业务-首页数据========test")
@RequestMapping("/sys/index")
@Slf4j
public class IndexController extends AbstractController {

    @Autowired
    StatisticsBusinessHelper businessHelper;

    @Autowired
    OrderService orderService;

    @GetMapping
    @ApiOperation(value = "商户、通道图形报表")
    public Result one(IndexReqParams reqParams) {
        long start = System.currentTimeMillis();
        List<DataRespParams> execute = businessHelper.buildBase(reqParams).execute();
        long end = System.currentTimeMillis();
        log.info("<<<<<<<<<<<<<<<<<<<<<<<" + (end - start));
        return Result.success(execute);
    }

    @GetMapping("/aaa")
    @ApiOperation(value = "top统计")
    public Result two() {
        return Result.success();
    }
}
