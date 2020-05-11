package com.pay.manager.pc.log;

import com.pay.common.annotation.Query;
import com.pay.common.enums.LogType;
import com.pay.data.querybase.BaseQueryCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogQuery extends BaseQueryCriteria{

    @Query(type = Query.Type.INNER_LIKE, propName = "username")
    private String userName;

    @Query(type = Query.Type.EQUAL, propName = "logType")
    private LogType logType;

    @Query(type = Query.Type.INNER_LIKE, propName = "method")
    private  String method;

    @Query(type = Query.Type.BETWEEN)
    private String createTime;

}
