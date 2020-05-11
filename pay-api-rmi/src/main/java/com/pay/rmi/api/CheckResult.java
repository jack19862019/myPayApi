package com.pay.rmi.api;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.rmi.api.req.OrderReqParams;
import lombok.Data;

@Data
public class CheckResult {

    private McpConfigEntity mcpConfigEntity;

    private MerchantEntity merchant;

    private ChannelEntity channel;

    private OrderReqParams reqParams;
}
