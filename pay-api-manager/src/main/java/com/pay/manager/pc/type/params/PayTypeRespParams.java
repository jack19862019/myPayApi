package com.pay.manager.pc.type.params;

import lombok.Data;

@Data
public class PayTypeRespParams {

    //支付方式名称
    private String payTypeName;

    //支付方式 标识
    private String payTypeFlag;

    //备注
    private String remark;
}
