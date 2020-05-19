package com.pay.gateway.controller.business;

import com.pay.common.annotation.SysParamsValidator;
import com.pay.common.page.PageReqParams;
import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.type.PayTypeService;
import com.pay.manager.pc.type.params.PayTypeParams;
import com.pay.manager.pc.type.params.PayTypeQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@Api(tags = "业务-支付方式接口")
@RequestMapping("/sys/pay/type/")
public class PayTypeController extends AbstractController {

    @PostMapping
    @SysParamsValidator
    @ApiOperation(value = "新增支付方式")
    public Result addPayType(@RequestBody PayTypeParams reqParams) {
        payTypeService.insert(reqParams);
        return Result.success();
    }

    @PutMapping("{id}")
    @SysParamsValidator
    @ApiOperation(value = "修改支付方式")
    public Result updatePayType(@RequestBody PayTypeParams reqParams, @PathVariable Long id) {
        payTypeService.update(reqParams, id);
        return Result.success();
    }

    @DeleteMapping("{id}")
    @ApiOperation(value = "删除支付方式")
    public Result deletePayType(@PathVariable Long id) {
        payTypeService.delete(id);
        return Result.success();
    }

    @GetMapping("{id}")
    @ApiOperation(value = "支付方式详情")
    public Result getPayType(@PathVariable Long id) {
        return Result.success(payTypeService.select(id));
    }

    @GetMapping("page")
    @ApiOperation(value = "支付方式分页")
    public Result getPayTypePage(PayTypeQuery query, PageReqParams reqParams) {
        return Result.success(payTypeService.selectPage(query, reqParams));
    }


    @Autowired
    PayTypeService payTypeService;
}
