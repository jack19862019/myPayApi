package com.pay.manager.pc.channel.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChannelReqParams {

    @NotBlank(message = "通道名称不能为空")
    private String channelName;

    @NotBlank(message = "通道标识不能为空")
    private String channelFlag;

    @NotBlank(message = "上游支付地址不能为空")
    private String upPayUrl;

    private String remark;


}
