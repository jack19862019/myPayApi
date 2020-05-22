package com.pay.manager.pc.upPayType.params;

import lombok.Data;

@Data
public class UpPayTypeRespParams {

    //支付方式名称
    private String upPayTypeName;

    //支付方式 标识
    private String upPayTypeFlag;

    //备注
    private String remark;
}
