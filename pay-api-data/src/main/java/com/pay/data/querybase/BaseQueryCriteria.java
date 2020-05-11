package com.pay.data.querybase;

import com.pay.common.annotation.Query;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BaseQueryCriteria {

    @ApiModelProperty(hidden = true)
    @Query
    private Long id;

    @ApiModelProperty(hidden = true)
    @Query(type = Query.Type.INNER_LIKE)
    private String name;

    @ApiModelProperty(hidden = true)
    @Query
    private String createUser;

}
