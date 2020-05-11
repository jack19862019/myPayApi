package com.pay.manager.pc.user.params;

import com.pay.common.annotation.Query;
import com.pay.data.querybase.BaseQueryCriteria;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserQuery extends BaseQueryCriteria{

    @Query(type = Query.Type.INNER_LIKE, propName = "username")
    private String userName;

    @Query(type = Query.Type.INNER_LIKE)
    private String phone;

    @Query(type = Query.Type.EQUAL, propName = "role.id")
    private String roleId;

    @ApiModelProperty(hidden = true)
    @Query(type = Query.Type.NOT_EQUAL, propName = "username")
    private String notShow;
}
