package com.pay.gateway.controller.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.annotation.SysParamsValidator;
import com.pay.common.enums.EncryptionType;
import com.pay.common.exception.Assert;
import com.pay.common.utils.Result;
import com.pay.gateway.controller.AbstractController;
import com.pay.manager.pc.mcp.McpConfigService;
import com.pay.manager.pc.mcp.params.McpConfigReqParams;
import com.pay.manager.pc.mcp.params.McpQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.pay.common.constant.Constant.RSA_PRI_KEY;
import static com.pay.common.constant.Constant.RSA_PUB_KEY;


@RestController
@Api(tags = "业务-商户->通道->支付方式->配置接口")
@RequestMapping("/sys/McpConfig/")
public class McpConfigController extends AbstractController {

    @PostMapping
    @SysParamsValidator
    @ApiOperation(value = "新增MCP配置")
    public Result addMcpConfig(@RequestBody McpConfigReqParams reqParams) {
        if (reqParams.getEncryptionType().equals(EncryptionType.RSA)) {
            Map<String, String> map = JSON.parseObject(reqParams.getUpKey(), new TypeReference<Map<String, String>>() {
            });
            Assert.mustBeTrue(
                    map.containsKey(RSA_PRI_KEY) &&
                            map.containsKey(RSA_PUB_KEY) &&
                            !map.get(RSA_PRI_KEY).isEmpty() &&
                            !map.get(RSA_PUB_KEY).isEmpty(),
                    "RSA密钥不完整"
            );
        }
        mcpConfigService.insert(reqParams);
        return Result.success();
    }

    @PutMapping("merchant/channel")
    @SysParamsValidator
    @ApiOperation(value = "修改MCP商户通道配置")
    public Result updateMcpConfig(@RequestBody McpConfigReqParams reqParams) {
        mcpConfigService.update(reqParams);
        return Result.success();
    }

    @DeleteMapping("{id}")
    @ApiOperation(value = "删除MCP配置")
    public Result deleteMcpConfig(@PathVariable Long id) {
        mcpConfigService.delete(id);
        return Result.success();
    }

    @GetMapping("{id}")
    @ApiOperation(value = "MCP配置详情")
    public Result getMcpConfig(@PathVariable Long id) {
        return Result.success(mcpConfigService.select(id));
    }

    @GetMapping("{merchantId}/mcp/list")
    @ApiOperation(value = "MCP配置列表")
    public Result getMcpChannels(@PathVariable Long merchantId) {
        return Result.success(mcpConfigService.getMcpChannels(merchantId));
    }

    @GetMapping("mcp/detail")
    @ApiOperation(value = "MCP商户通道详情")
    public Result getMcpChannelDetail(McpQuery mcpQuery) {
        return Result.success(mcpConfigService.getMcpChannelDetail(mcpQuery));
    }

    @Autowired
    McpConfigService mcpConfigService;
}
