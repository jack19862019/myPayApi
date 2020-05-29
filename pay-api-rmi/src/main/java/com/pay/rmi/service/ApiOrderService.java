package com.pay.rmi.service;

import com.pay.data.entity.OrderEntity;

public interface ApiOrderService {

    OrderEntity selectByMerchNoAndOrderNo(String merchNo, String orderNo);

    void save(OrderEntity order);

    void update(OrderEntity order);
}
