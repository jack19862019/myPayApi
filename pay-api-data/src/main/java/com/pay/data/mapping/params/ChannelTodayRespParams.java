package com.pay.data.mapping.params;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
//@BeanCopySource(source = ChannelEntity.class)
public class ChannelTodayRespParams {

    private Integer hour;

    private String day;

    private String dateDisplay;

    private Integer monthNum;

    private BigDecimal amount;

}
