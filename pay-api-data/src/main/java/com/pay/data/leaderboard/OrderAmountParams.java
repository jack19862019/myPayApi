package com.pay.data.leaderboard;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class OrderAmountParams {
    private BigDecimal totalAmount;

    private String channelFlag;
}
