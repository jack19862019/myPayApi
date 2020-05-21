package com.pay.manager.pc.channel.params;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class ChannelReqMapReqParams {

    @Valid
    @NotEmpty(message = "通道配置不能为空")
    private List<ChannelReqIndexParams> indexParams;
}
