package com.pay.rmi.service;

import com.pay.data.entity.ChannelEntity;

public interface ApiChannelService {

    ChannelEntity selectByChannelNo(String channelNo);
}
