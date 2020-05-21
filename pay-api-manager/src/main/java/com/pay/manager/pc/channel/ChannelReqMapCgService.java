package com.pay.manager.pc.channel;

import com.pay.manager.pc.channel.params.ChannelReqIndexParams;
import com.pay.manager.pc.channel.params.ChannelReqMapReqParams;

import java.util.List;

public interface ChannelReqMapCgService {

    List<ChannelReqIndexParams> getThisReqParams();

    void saveChannelConfig(Long channelId, ChannelReqMapReqParams reqParams);

    List<ChannelReqIndexParams> getChannelConfig(Long id);

}
