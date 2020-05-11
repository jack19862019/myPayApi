package com.pay.manager.pc.statistics.params;

import lombok.Data;

@Data
public class DataRespParams {

    private XDateRespParams xDateRespParams;

    private YDataRespParams yDataRespParams;

    public DataRespParams(XDateRespParams xDateRespParams, YDataRespParams yDataRespParams) {
        this.xDateRespParams = xDateRespParams;
        this.yDataRespParams = yDataRespParams;
    }
}
