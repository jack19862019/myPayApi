package com.pay.manager.pc.mcp.params;

import lombok.Data;

import java.util.List;

@Data
public class McpChannelParams {

    private Long id;

    //支付通道名称
    private String channelName;

    //支付通道标识（名称的拼音首字母）
    private String channelFlag;

    //备注
    private String remark;
}
