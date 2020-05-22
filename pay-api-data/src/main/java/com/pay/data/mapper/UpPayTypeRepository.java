package com.pay.data.mapper;

import com.pay.data.entity.PayTypeEntity;
import com.pay.data.entity.UpPayTypeEntity;


public interface UpPayTypeRepository extends BaseRepository<UpPayTypeEntity, Long> {

    UpPayTypeEntity findByUpPayTypeFlag(String upPayTypeFlag);

}
