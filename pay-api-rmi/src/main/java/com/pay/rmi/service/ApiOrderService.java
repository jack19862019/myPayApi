package com.pay.rmi.service;

import com.pay.data.entity.OrderEntity;
import com.pay.rmi.api.resp.OrderListResp;
import org.springframework.data.domain.Page;

public interface ApiOrderService {

    OrderEntity selectByMerchNoAndOrderNo(String merchNo, String orderNo);

    OrderEntity save(OrderEntity order);

    OrderEntity update(OrderEntity order);

    Page<OrderListResp> selectByFlagAndOrderNo(String orderNo, String pageSize, String pageNum);
}
