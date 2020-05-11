package com.pay.manager.pc.paylog;

import com.pay.common.annotation.Query;
import com.pay.data.querybase.BaseQueryCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayLogQuery extends BaseQueryCriteria{

    private String methodsName;
    @Query(type = Query.Type.INNER_LIKE, propName = "channelFlag")
    private String channelFlag;
    @Query(type = Query.Type.INNER_LIKE, propName = "channelName")
    private String channelName;
    @Query(type = Query.Type.INNER_LIKE, propName = "logContent")
    private String logContent;
}
