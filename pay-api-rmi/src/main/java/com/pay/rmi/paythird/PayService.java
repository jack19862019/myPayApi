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

    String callback(OrderEntity order, Map<String, String> params);


}
