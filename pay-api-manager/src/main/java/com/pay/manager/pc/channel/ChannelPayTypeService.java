package com.pay.manager.pc.channel;

import com.pay.manager.pc.channel.params.ChannelPayTypeParams;

import java.util.List;

public interface ChannelPayTypeService {

    void insert(Long channelId, List<ChannelPayTypeParams> reqParamsList);

    void update(Long channelId, List<ChannelPayTypeParams> reqParamsList);
}
