package com.pay.manager.pc.mcp.params;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class McpConfigRespParams {

    private McpMerchantParams merchantParams;

    private List<McpChannelParams> channelParams;

    public McpConfigRespParams(McpMerchantParams merchantParams, List<McpChannelParams> channelParams) {
        this.merchantParams = merchantParams;
        this.channelParams = channelParams;
    }
}
