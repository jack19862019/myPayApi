package com.pay.data.mapper;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.UpPayTypeEntity;

import java.util.List;


public interface UpPayTypeRepository extends BaseRepository<UpPayTypeEntity, Long> {

    UpPayTypeEntity findByPayType_PayTypeFlagAndChannel_ChannelFlag(String payTypeFlag, String channelFlag);

    List<UpPayTypeEntity> findAllByChannel(ChannelEntity channelEntity);

}
