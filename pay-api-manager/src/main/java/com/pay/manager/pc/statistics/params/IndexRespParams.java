package com.pay.manager.pc.statistics.params;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class IndexRespParams {

    private Long merchantCount;

    private Long channelCount;

    private Long orderCount;

    private BigDecimal orderAmountCount;

    private List<DataRespParams> mocList;

    //private List<Date>
}
