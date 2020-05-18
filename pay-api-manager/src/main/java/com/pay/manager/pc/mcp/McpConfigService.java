package com.pay.manager.pc.mcp;

import com.pay.manager.pc.mcp.params.*;

public interface McpConfigService {

    void insert(McpConfigReqParams reqParams);

    McpConfigDetailParams select(Long id);

    void update(McpConfigReqParams reqParams);

    void delete(Long id);

    McpConfigRespParams getMcpChannels(Long merchantId);

    McpConfigDetailParams getMcpChannelDetail(McpQuery mcpQuery);
}
