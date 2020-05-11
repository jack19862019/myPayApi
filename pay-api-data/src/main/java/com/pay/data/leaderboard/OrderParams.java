package com.pay.data.leaderboard;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderParams {
    //入住商户
    private long countMerchant;
    //支付通道
    private long countChannel;
    //商户订单
    private long countOrder;
    //充值金额
    private BigDecimal countOrderAmount;
    //支付方式排行榜
    List<OrderTypeAmountParams> orderTypeAmountParams;
    //通道排行榜
    List<OrderAmountParams> orderAmountParams;
}
