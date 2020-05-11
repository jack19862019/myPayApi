package com.pay.manager.pc.statistics;

import com.pay.common.enums.DateIntervalType;
import com.pay.common.enums.MerChaType;
import com.pay.manager.pc.statistics.params.DataRespParams;

import java.math.BigDecimal;
import java.util.List;

public interface StatisticsService {

    Long getCountOrder();

    BigDecimal getCountOrderAmount();

    List<DataRespParams> getIndexHistogram(String mocFlag, DateIntervalType dateIntervalType, String dateScope, MerChaType merChaType);

}
