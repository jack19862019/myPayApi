package com.pay.data.mapper;

import com.pay.data.entity.PayLogEntity;

import java.util.List;

/**
 * 支付日志
 */
public interface PayLogRepository extends BaseRepository<PayLogEntity, Long> {

    List<PayLogEntity> findAllByOrderNo(String orderNo);

}
