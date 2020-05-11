package com.pay.rmi.service;


import com.pay.data.entity.ChannelEntity;
import com.pay.data.mapper.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiChannelServiceImpl implements ApiChannelService  {

    @Autowired
    private ChannelRepository channelRepository;

    @Override
    public ChannelEntity selectByChannelNo(String channelNo) {
        return channelRepository.findByChannelFlag(channelNo);
    }
}
