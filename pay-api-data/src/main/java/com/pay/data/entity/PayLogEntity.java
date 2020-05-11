package com.pay.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "pay_log",
        indexes = {
                @Index(columnList = "channelName"),
                @Index(columnList = "channelFlag"),
                @Index(name = "channel_flag_channel_name", columnList = "channelName"),
                @Index(name = "channel_flag_channel_name", columnList = "channelFlag"),
        })
public class PayLogEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private Date createTime;

    private String channelFlag;

    private String channelName;

    @Column(columnDefinition = "text")
    private String logContent;
}
