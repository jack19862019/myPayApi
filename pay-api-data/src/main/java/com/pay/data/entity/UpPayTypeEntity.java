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
@Table(name = "upPayType",
        uniqueConstraints = {@UniqueConstraint(columnNames={"upPayTypeName", "upPayTypeFlag"})},
        indexes = {
                @Index(columnList = "upPayTypeName"),
                @Index(columnList = "upPayTypeFlag", unique = true),
        })
public class UpPayTypeEntity extends BaseEntity {

    //支付方式名称
    private String upPayTypeName;

    //支付方式 标识
    private String upPayTypeFlag;

    //备注
    private String upRemark;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "channelFlag", referencedColumnName = "channelFlag")
    private ChannelEntity channelEntity;

    @OneToMany(mappedBy = "upPayTypeEntity")
    private Set<McpPayTypeEntity> mcpPayType;

    @OneToMany(mappedBy = "upPayTypeEntity")
    private Set<OrderEntity> orders;

    //平台支付方式ID
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "payTypeFlag",referencedColumnName = "payTypeFlag")
    private PayTypeEntity payTypeEntity;
}