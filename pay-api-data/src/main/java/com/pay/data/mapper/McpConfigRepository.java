package com.pay.data.mapper;

import com.pay.data.entity.McpConfigEntity;

import java.util.List;


public interface McpConfigRepository extends BaseRepository<McpConfigEntity, Long> {

    McpConfigEntity findByMerchant_MerchantNoAndChannel_ChannelFlag(String merchantNo, String channelFlag);

    List<McpConfigEntity> findAllByMerchant_MerchantNo(String merchantNo);

    McpConfigEntity findByChannel_IdAndMerchant_Id(Long channelId, Long merchantId);
}
