package com.pay.manager.pc.role.params;

import com.pay.common.annotation.Query;
import com.pay.common.constant.Constant;
import com.pay.data.querybase.BaseQueryCriteria;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleQuery extends BaseQueryCriteria {

    @Query(blurry = "name,num")
    private String blurry;

    @ApiModelProperty(hidden = true)
    @Query(type = Query.Type.NOT_EQUAL)
    private String num = Constant.JS001;

}
