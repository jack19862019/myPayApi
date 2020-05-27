package com.pay.rmi.paythird;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;

import java.util.Map;

public interface PayService {

    OrderApiRespParams order(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams);

    Map<String, String> requestToUpParams(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams);

    String signToUp(String context);

    String httpRequestUp(String payUrl, Map<String, String> requestToUpParams);

    OrderApiRespParams returnRespToDown(String result);

    String callback(ChannelEntity channel, MerchantEntity merchant, McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params);


}
