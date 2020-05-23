package com.pay.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Where(clause = "is_delete=1")
@Table(name = "upPayType",
        indexes = {
                @Index(columnList = "upPayTypeName"),
                @Index(columnList = "upPayTypeFlag"),
        })
public class UpPayTypeEntity extends BaseEntity {

    //支付方式名称
    private String upPayTypeName;

    //支付方式 标识
    private String upPayTypeFlag;

    @ManyToOne(cascade = CascadeType.REFRESH)
    private ChannelEntity channel;

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "upPayType")
    private Set<OrderEntity> orders = new HashSet<>();

    //平台支付方式ID
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "payTypeId", referencedColumnName = "id")
    private PayTypeEntity payType;
}
