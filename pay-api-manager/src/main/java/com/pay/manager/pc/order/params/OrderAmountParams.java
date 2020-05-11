package com.pay.manager.pc.order.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class OrderAmountParams {

    private BigDecimal totalAmount;

    private String channelFlag;
}
