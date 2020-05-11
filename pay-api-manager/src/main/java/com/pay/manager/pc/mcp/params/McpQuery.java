package com.pay.manager.pc.mcp.params;

import com.pay.common.annotation.Query;
import com.pay.data.querybase.BaseQueryCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class McpQuery extends BaseQueryCriteria {

    @Query(propName = "merchant.id")
    private Long merchantId;

    @Query(propName = "channel.id")
    private Long channelId;

    public McpQuery() {
    }

    public McpQuery(Long merchantId) {
        this.merchantId = merchantId;
    }
}
