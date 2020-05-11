package com.pay.manager.pc.order.params;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pay.common.enums.NotifyStatus;
import com.pay.common.enums.OrderStatus;
import com.pay.common.enums.OrderType;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.entity.PayTypeEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Convert;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.Date;

@Data
@BeanCopySource(source = OrderEntity.class)
public class OrderPageRespParams {
    //订单号
    private String orderNo;

    //下游商户号
    @CopyProperty(property = "merchant.merchantNo")
    private String merchantNo;

    //下游商户号
    @CopyProperty(property = "merchant.merchantName")
    private String merchantName;

    //支付平台名称（通道名称）
    @CopyProperty(property = "channel.channelName")
    private String channelName;

    //支付方式名称
    @CopyProperty(property = "payType.payTypeName")
    private String payTypeName;

    //订单金额
    private BigDecimal orderAmount;

    //实际支付金额
    private BigDecimal realAmount;

    //订单状态（枚举）
    private OrderStatus orderStatus;

    //通知状态（枚举）
    private NotifyStatus notifyStatus;

    //请求时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //更新时间
    private Date updateTime;

    private String businessNo;
}
