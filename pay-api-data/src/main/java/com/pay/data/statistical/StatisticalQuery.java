package com.pay.data.statistical;

import com.pay.common.annotation.Query;
import com.pay.common.enums.DateIntervalType;
import com.pay.data.querybase.BaseQueryCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticalQuery extends BaseQueryCriteria{

    private DateIntervalType dateType;

    private String day;

    @Query(type = Query.Type.INNER_LIKE, propName = "merchantNo")
    private String merchantNo;

}
