package com.pay.manager.pc.statistics;

import com.pay.common.enums.DateIntervalType;
import com.pay.common.enums.MerChaType;
import com.pay.manager.pc.statistics.params.DataRespParams;
import com.pay.manager.pc.statistics.params.IndexReqParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StatisticsBusinessHelper {

    private DateIntervalType dateIntervalType;

    private String dateScope;

    private String mocFlag;

    private MerChaType merChaType;


    public StatisticsBusinessHelper buildBase(IndexReqParams reqParams) {
        this.dateIntervalType = reqParams.getDateIntervalType();
        this.dateScope = reqParams.getDateScope();
        this.mocFlag = reqParams.getMocFlag();
        this.merChaType = reqParams.getMerChaType();
        return this;
    }

    public List<DataRespParams> execute() {
        return statisticsService.getIndexHistogram(mocFlag, dateIntervalType, dateScope, merChaType);
    }

    @Autowired
    StatisticsService statisticsService;
}
