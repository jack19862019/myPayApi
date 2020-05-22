package com.pay.manager.pc.upPayType.params;

import com.pay.common.annotation.Query;
import lombok.Data;

@Data
public class UpPayTypeQuery {

    @Query(type = Query.Type.INNER_LIKE)
    private String channelFlag;
}
