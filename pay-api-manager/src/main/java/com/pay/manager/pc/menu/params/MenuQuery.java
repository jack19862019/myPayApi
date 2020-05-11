package com.pay.manager.pc.menu.params;

import com.pay.common.annotation.Query;
import com.pay.data.querybase.BaseQueryCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuQuery extends BaseQueryCriteria {

    @Query(propName = "parent.id")
    private Long parentId;

}
