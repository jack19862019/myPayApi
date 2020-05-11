package com.pay.manager.pc.merchant.params;

import com.pay.common.annotation.Query;
import com.pay.data.querybase.BaseQueryCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchantQuery extends BaseQueryCriteria {

    @Query(type = Query.Type.INNER_LIKE)
    private String merchantNo;

    @Query(type = Query.Type.INNER_LIKE)
    private String merchantName;

    @Query(type = Query.Type.INNER_LIKE)
    private String nickName;
}
