package com.pay.manager.pc.upPayType.params;

import lombok.Data;

@Data
public class UpPayTypePageRespParams {

    private Long id;

    private String UpPayTypeName;

    private String UpPayTypeFlag;

    private String payTypeName;

    private String payTypeFlag;
}
