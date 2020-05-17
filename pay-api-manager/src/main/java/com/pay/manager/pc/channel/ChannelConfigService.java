package com.pay.manager.pc.channel;

import com.pay.manager.pc.channel.params.ChannelConfigIndexParams;
import com.pay.manager.pc.channel.params.ChannelConfigReqParams;

import java.util.List;

public interface ChannelConfigService {

    List<ChannelConfigIndexParams> getThisReqParams();

    void insertChannelConfig(ChannelConfigReqParams reqParams);

    List<ChannelConfigIndexParams> getChannelConfig(Long id);

    void updateChannelConfig(ChannelConfigReqParams reqParams);
}
