package com.pay.manager.pc.channel.params;

import lombok.Data;

@Data
public class UpPayTypeReqParams {

    private String upPayTypeName;

    private String upPayTypeFlag;

    private Long payTypeId;

}
