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
@Table(name = "payType",
        uniqueConstraints = {@UniqueConstraint(columnNames={"payTypeName", "payTypeFlag"})},
        indexes = {
                @Index(columnList = "payTypeName"),
                @Index(columnList = "payTypeFlag", unique = true),
        })
public class PayTypeEntity extends BaseEntity {

    //支付方式名称
    private String payTypeName;

    //支付方式 标识
    private String payTypeFlag;

    //备注
    private String remark;

    /*@ManyToMany(mappedBy = "payTypes")
    private Set<ChannelEntity> channels;

    @OneToMany(mappedBy = "payType")
    private Set<McpPayTypeEntity> mcpPayType;

    @OneToMany(mappedBy = "payType")
    private Set<OrderEntity> orders;*/

    @OneToMany(mappedBy = "payType")
    private Set<UpPayTypeEntity> upPayTypeEntities;
}
