package com.pay.data.mapper;

import com.pay.data.entity.ChannelContactEntity;

import java.util.List;

/**
 * 系统日志
 */
public interface ChannelContactRepository extends BaseRepository<ChannelContactEntity, Long> {

    List<ChannelContactEntity> findByChannel_Id(Long channelId);

    void deleteAllByChannel_Id(Long channelId);
}
