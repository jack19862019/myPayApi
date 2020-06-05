package com.pay.rmi.paythird;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.params.OrderReqParams;

import java.util.Map;

public interface ParamsService {

    Map<String, String> requestToUpParams(OrderReqParams reqParams);

    String signToUp(String context, String upKey);

}
