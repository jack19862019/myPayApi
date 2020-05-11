package com.pay.gateway.controller.business;

import com.pay.common.annotation.SysParamsValidator;
import com.pay.common.page.PageReqParams;
import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.merchant.params.MerchantQuery;
import com.pay.manager.pc.merchant.params.MerchantReqParams;
import com.pay.manager.pc.merchant.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Api(tags = "业务-商户管理接口")
@RequestMapping("/sys/merchant/")
public class MerchantController extends AbstractController {

    @PostMapping
    @SysParamsValidator
    @ApiOperation(value = "新增商户")
    public Result addMerchant(@RequestBody MerchantReqParams reqParams) {
        merchantService.insert(reqParams);
        return Result.success();
    }

    @PutMapping("{id}")
    @SysParamsValidator
    @ApiOperation(value = "修改商户")
    public Result updateMerchant(@RequestBody MerchantReqParams reqParams, @PathVariable Long id) {
        merchantService.update(reqParams, id);
        return Result.success();
    }

    @DeleteMapping("{id}")
    @ApiOperation(value = "删除商户")
    public Result deleteMerchant(@PathVariable Long id) {
        merchantService.delete(id);
        return Result.success();
    }

    @GetMapping("{id}")
    @ApiOperation(value = "商户详情")
    public Result getMerchant(@PathVariable Long id) {
        return Result.success(merchantService.select(id));
    }

    @GetMapping("page")
    @ApiOperation(value = "商户分页")
    public Result getMerchantPage(MerchantQuery query, @Validated PageReqParams reqParams) {
        return Result.success(merchantService.selectPage(query, reqParams));
    }


    @Autowired
    MerchantService merchantService;
}
