package com.pay.manager.pc.channel.params;

import com.pay.common.annotation.Query;
import com.pay.data.querybase.BaseQueryCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelQuery extends BaseQueryCriteria {

    @Query(type = Query.Type.INNER_LIKE)
    private String channelFlag;

    @Query(type = Query.Type.INNER_LIKE)
    private String channelName;

    @Query
    private String merchantId;

}
