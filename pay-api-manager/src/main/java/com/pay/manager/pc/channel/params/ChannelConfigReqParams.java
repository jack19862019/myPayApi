package com.pay.manager.pc.channel.params;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ChannelConfigReqParams {

    @Valid
    @NotEmpty(message = "通道配置不能为空")
    private List<ChannelConfigIndexParams> indexParams;
}
