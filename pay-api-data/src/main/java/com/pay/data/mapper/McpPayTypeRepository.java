package com.pay.data.mapper;

import com.pay.data.entity.McpPayTypeEntity;


public interface McpPayTypeRepository extends BaseRepository<McpPayTypeEntity, Long> {

    McpPayTypeEntity findByMcpConfig_IdAndPayType_Id(Long configId, Long payTypeId);
}
