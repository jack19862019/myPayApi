package com.pay.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Where(clause = "is_delete=1")
@Table(name = "upMerchantPayType",
        uniqueConstraints = {@UniqueConstraint(columnNames={"upPayTypeName", "upPayTypeFlag"})},
        indexes = {
                @Index(columnList = "upPayTypeName"),
                @Index(columnList = "upPayTypeFlag", unique = true),
        })
public class UpMerchantPayTypeEntity extends BaseEntity {

    //支付方式名称
    private String upPayTypeName;

    //支付方式 标识
    private String upPayTypeFlag;

    //备注
    private String upRemark;

    @ManyToMany(mappedBy = "upPayTypes")
    private Set<ChannelEntity> channels;

    @OneToMany(mappedBy = "upMerchantPayTypeEntity")
    private Set<McpPayTypeEntity> mcpPayType;

    @OneToMany(mappedBy = "payTypeEntity")
    private Set<OrderEntity> orders;

    //平台支付方式ID
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "pay_type_id")
    private PayTypeEntity payTypeEntity;
}