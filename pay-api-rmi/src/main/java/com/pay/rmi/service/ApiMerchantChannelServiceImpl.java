package com.pay.rmi.service;


import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.mapper.ChannelRepository;
import com.pay.data.mapper.McpConfigRepository;
import com.pay.rmi.api.resp.ChannelResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class ApiMerchantChannelServiceImpl implements ApiMerchantChannelService {

    @Autowired
    private McpConfigRepository mcpConfigRepository;


    @Override
    public McpConfigEntity selectByMerchantNoAndChannelNo(String merchantNo, String channelNo) {
        return mcpConfigRepository.findByMerchant_MerchantNoAndChannel_ChannelFlag(merchantNo, channelNo);
    }

    @Override
    public List<ChannelResp> selectByMerchantNo(String merchantNo) {
        List<McpConfigEntity> mcpConfigEntities = mcpConfigRepository.findAllByMerchant_MerchantNo(merchantNo);
        List<ChannelEntity> collect = mcpConfigEntities.stream().map(McpConfigEntity::getChannel).collect(toList());
        List<ChannelResp> list = new ArrayList<>();
        for (ChannelEntity channelEntity : collect) {
            ChannelResp channelResp = new ChannelResp();
            channelResp.setNum(channelEntity.getChannelFlag());
            channelResp.setName(channelEntity.getChannelName());
            channelResp.setId(channelEntity.getId());
            list.add(channelResp);
        }
        return list;
    }
}
