package com.pay.manager.pc.order.params;

import com.pay.common.enums.IsOrder;
import com.pay.common.enums.IsValue;
import lombok.Data;

@Data
public class OrderLogParams {

    private String channelFlag;

    private String orderNo;

    private String method;

    private IsValue isValue;

    private String rGinseng;

    private String cGinseng;

    private Integer sort;

    private IsOrder isOrder;

}
