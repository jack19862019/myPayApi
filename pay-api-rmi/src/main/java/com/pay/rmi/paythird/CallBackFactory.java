package com.pay.rmi.paythird;

import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.pay.order.delay.NotifyTask;
import com.pay.rmi.service.ApiOrderService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class CallBackFactory {

    @Autowired
    protected NotifyTask notifyTask;

    @Autowired
    protected ApiOrderService orderService;

    protected McpConfigEntity mcpConfig;
    protected OrderEntity order;

    protected Map<String, String> callBackParams;
}
