package com.pay.manager.pc.mcp.params;

import com.pay.common.enums.AmountType;
import lombok.Data;

@Data
public class McpAmountReqParams {

    private String amountStr;

    private AmountType amountType;
}
