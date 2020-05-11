package com.pay.data.mapper;

import com.pay.data.entity.ChannelEntity;

/**
 * 系统日志
 */
public interface ChannelRepository extends BaseRepository<ChannelEntity, Long> {

    ChannelEntity findByChannelFlag(String channelFlag);
}
