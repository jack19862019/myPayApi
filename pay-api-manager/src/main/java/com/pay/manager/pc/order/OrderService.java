package com.pay.manager.pc.order;


import com.pay.common.page.PageReqParams;
import com.pay.manager.pc.order.params.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    Page<OrderPageRespParams> selectOrderPage(OrderQuery orderQuery, PageReqParams reqParams);

    void sendCallback(OrderPageReqParams reqParams);

    List<OrderAmountParams> getOrderAmountByChannelFlag(int page);

    OrderDetailBaseParams getOrderDetail(Long id);
}
