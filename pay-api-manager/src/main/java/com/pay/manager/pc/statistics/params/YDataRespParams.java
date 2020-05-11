package com.pay.manager.pc.statistics.params;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class YDataRespParams {

    private BigDecimal amount;

    public YDataRespParams(BigDecimal amount) {
        this.amount = amount;
    }
}
