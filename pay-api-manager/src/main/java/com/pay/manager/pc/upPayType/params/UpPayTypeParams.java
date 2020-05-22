package com.pay.manager.pc.upPayType.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpPayTypeParams {

    private Long id;

    @NotBlank(message = "上游支付方式名称不能为空")
    private String upPayTypeName;

    @NotBlank(message = "上游支付方式标识不能为空")
    private String upPayTypeFlag;

    @NotBlank(message = "通道标识不能为空")
    private String channelFlag;

    @NotBlank(message = "平台支付方式标识不能为空")
    private String payTypeFlag;
}
