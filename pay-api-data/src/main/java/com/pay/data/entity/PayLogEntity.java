package com.pay.data.entity;

import com.pay.common.enums.IsOrder;
import com.pay.common.enums.IsValue;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "pay_log",
        indexes = {
                @Index(columnList = "channelFlag"),
                @Index(columnList = "orderNo")})
public class PayLogEntity extends BaseEntity {

    private String channelFlag;

    private String orderNo;

    private String method;

    private IsValue isValue;

    @Column(columnDefinition = "text")
    private String rGinseng;

    @Column(columnDefinition = "text")
    private String cGinseng;

    private Integer sort;

    private IsOrder isOrder;

}

