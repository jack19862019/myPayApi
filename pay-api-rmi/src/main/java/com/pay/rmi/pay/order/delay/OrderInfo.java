package com.pay.rmi.pay.order.delay;

import com.pay.data.entity.OrderEntity;
import lombok.Data;

import java.util.Map;

@Data
public class OrderInfo {

    private OrderEntity order;

    private Map<String, String> params;

    private String originParams;

    private String notifyUrl;

    private int times;

    public OrderInfo(OrderEntity order, Map<String, String> params, String originParams, String notifyUrl) {
        this.order = order;
        this.params = params;
        this.originParams = originParams;
        this.notifyUrl = notifyUrl;
        this.times = 1;
    }
}
