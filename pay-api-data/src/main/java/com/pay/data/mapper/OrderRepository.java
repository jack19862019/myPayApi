package com.pay.data.mapper;

import com.pay.data.entity.OrderEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;


public interface OrderRepository extends BaseRepository<OrderEntity, Long> {

    OrderEntity findByMerchant_MerchantNoAndOrderNo(String merchantNo, String orderNo);

    @Query(value = "select COALESCE(sum(order_amount),0) from pay_order where merchant_no = :merchantNo and order_status = :orderStatus and create_time REGEXP :createTime", nativeQuery = true)
    BigDecimal merchantOrder(
            @Param("merchantNo") String merchantNo,
            @Param("orderStatus") Integer orderStatus,
            @Param("createTime") String createTime);

    @Query(value = "select COALESCE(sum(order_amount),0) from pay_order where channel_flag = :channelFlag and order_status = :orderStatus and create_time REGEXP :createTime", nativeQuery = true)
    BigDecimal channelOrder(
            @Param("channelFlag") String channelFlag,
            @Param("orderStatus") Integer orderStatus,
            @Param("createTime") String createTime);

}
