package com.pay.manager.pc.type.params;

import lombok.Data;

@Data
public class PayTypePageRespParams {

    private Long id;

    private String payTypeName;

    private String payTypeFlag;

    private String remark;
}
