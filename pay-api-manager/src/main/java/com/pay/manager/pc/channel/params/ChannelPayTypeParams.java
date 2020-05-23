package com.pay.manager.pc.channel.params;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChannelPayTypeParams {

    @NotNull(message = "平台通道支付方式必须配置")
    private Long payTypeId;

    @NotNull(message = "上游通道支付方式名称必须配置")
    private String upPayTypeName;

    @NotNull(message = "上游通道支付方式标识必须配置")
    private String upPayTypeFlag;

}
