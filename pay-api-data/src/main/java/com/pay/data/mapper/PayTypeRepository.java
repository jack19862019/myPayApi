package com.pay.data.mapper;

import com.pay.data.entity.PayTypeEntity;


public interface PayTypeRepository extends BaseRepository<PayTypeEntity, Long> {

    PayTypeEntity findByPayTypeFlag(String payTypeFlag);

}
