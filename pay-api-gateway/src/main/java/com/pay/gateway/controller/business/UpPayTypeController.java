package com.pay.gateway.controller.business;

import com.pay.common.annotation.SysParamsValidator;
import com.pay.common.page.PageReqParams;
import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.type.PayTypeService;
import com.pay.manager.pc.type.params.PayTypeParams;
import com.pay.manager.pc.type.params.PayTypeQuery;
import com.pay.manager.pc.upPayType.UpPayTypeService;
import com.pay.manager.pc.upPayType.params.UpPayTypeParams;
import com.pay.manager.pc.upPayType.params.UpPayTypeQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@Api(tags = "业务-上游支付方式接口")
@RequestMapping("/sys/pay/upPayType/")
public class UpPayTypeController extends AbstractController {

    @PostMapping
    @SysParamsValidator
    @ApiOperation(value = "新增上游支付方式")
    public Result addUpPayType(@RequestBody UpPayTypeParams upReqParams) {
        upPayTypeService.insert(upReqParams);
        return Result.success();
    }

    @PutMapping("{id}")
    @SysParamsValidator
    @ApiOperation(value = "修改上游支付方式")
    public Result updatePayType(@RequestBody UpPayTypeParams upReqParams, @PathVariable Long id) {
        upPayTypeService.update(upReqParams, id);
        return Result.success();
    }

    @DeleteMapping("{id}")
    @ApiOperation(value = "删除上游支付方式")
    public Result deletePayType(@PathVariable Long id) {
        upPayTypeService.delete(id);
        return Result.success();
    }

    @GetMapping("page")
    @ApiOperation(value = "上游支付方式分页")
    public Result getPayTypePage(UpPayTypeQuery query, PageReqParams reqParams) {
        return Result.success(upPayTypeService.selectPage(query, reqParams));
    }


    @Autowired
    UpPayTypeService upPayTypeService;
}
