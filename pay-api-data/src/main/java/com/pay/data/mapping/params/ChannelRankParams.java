package com.pay.data.mapping.params;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChannelRankParams {
    private String channelId;
    private String channelName;
    private BigDecimal sumAmount;
}
