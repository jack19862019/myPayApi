package com.pay.manager.pc.channel.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ChannelPayTypeParams {

    @NotNull(message = "通道支付方式必须配置")
    private Long id;
}
