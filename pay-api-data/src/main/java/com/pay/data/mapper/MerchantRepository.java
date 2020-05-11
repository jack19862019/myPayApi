package com.pay.data.mapper;

import com.pay.data.entity.MerchantEntity;
import org.springframework.stereotype.Repository;

public interface MerchantRepository extends BaseRepository<MerchantEntity, Long> {

    MerchantEntity findByMerchantNo(String merchantNo);
}
