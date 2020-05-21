package com.pay.manager.pc.channel;

import com.pay.manager.pc.channel.params.ChannelConfigIndexParams;
import com.pay.manager.pc.channel.params.ChannelConfigReqParams;

import java.util.List;

public interface ChannelReqParamsCgService {

    List<ChannelConfigIndexParams> getThisReqParams();

    void saveChannelConfig(Long channelId, ChannelConfigReqParams reqParams);

    List<ChannelConfigIndexParams> getChannelConfig(Long id);

}
