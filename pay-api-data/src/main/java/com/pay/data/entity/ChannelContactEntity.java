package com.pay.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "channel_contact")
public class ChannelContactEntity {

    @Id
    @GeneratedValue(generator = "IDGenerator")
    @GenericGenerator(name = "IDGenerator", strategy = "com.pay.common.utils.IDGenerator")
    private Long id;

    //联系方式key
    private String contactKey;

    //联系方式值
    private String contactValue;

    @ManyToOne(cascade = CascadeType.REFRESH)
    private ChannelEntity channel;
}
