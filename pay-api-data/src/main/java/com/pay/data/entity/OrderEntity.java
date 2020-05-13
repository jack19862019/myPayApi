package com.pay.data.entity;

import com.pay.common.enums.NotifyStatus;
import com.pay.common.enums.OrderStatus;
import com.pay.common.enums.OrderType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "pay_order",
        indexes = {
                @Index(columnList = "orderNo", unique = true),
                @Index(columnList = "merchantNo")
        }
)
public class OrderEntity extends BaseEntity {

    //订单号
    private String orderNo;

    //第三方返回的单号
    private String businessNo;

    //下游商户号
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "merchantNo", referencedColumnName = "merchantNo")
    private MerchantEntity merchant;

    //支付平台名称（通道名称）
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "channelFlag", referencedColumnName = "channelFlag")
    private ChannelEntity channel;

    //上游商户号
    private String upMerchantNo;

    //支付方式ID
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "up_pay_type_id")
    private PayTypeEntity payTypeEntity;

    //订单金额
    private BigDecimal orderAmount;

    //实际支付金额
    private BigDecimal realAmount;

    //订单状态（枚举）
    @Convert(converter = OrderStatus.Convert.class)
    private OrderStatus orderStatus = OrderStatus.init;

    //通知状态（枚举）
    @Convert(converter = NotifyStatus.Convert.class)
    private NotifyStatus notifyStatus = NotifyStatus.INIT;

    //订单类型（枚举）
    @Convert(converter = OrderType.Convert.class)
    private OrderType orderType = OrderType.PAY;

    //回调地址
    private String returnUrl;

    //通知地址
    private String notifyUrl;

    //产品名称
    private String product;

    //下游支付用户id
    private String userId;

    //请求ip
    private String reqIp;

    //备注信息
    private String memo;

    //银行卡号
    /*private String bankCode;

    private String bankAccountName;

    private String bankAccountNo;*/

}
