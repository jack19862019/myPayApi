package com.pay.manager.pc.type.params;

import com.pay.common.annotation.Query;
import lombok.Data;

@Data
public class PayTypeQuery {

    @Query(blurry = "payTypeName,payTypeFlag")
    private String blurry;
}
