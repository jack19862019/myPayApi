package com.pay.manager.pc.channel.params;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ChannelPayTypeParams {

    @NotNull(message = "平台通道支付方式必须配置")
    private Long payTypeId;

    @NotNull(message = "上游通道支付方式名称必须配置")
    private String upPayTypeName;

    @NotNull(message = "上游通道支付方式标识必须配置")
    private String upPayTypeFlag;

    @Valid
    @NotEmpty(message = "该通道的支付方式必须配置")
    private List<ChannelPayTypeParams> channelUpPayTypeParams;

}
