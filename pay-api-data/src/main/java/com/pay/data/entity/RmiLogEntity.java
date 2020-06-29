package com.pay.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "rmi_log",
        indexes = {
                @Index(columnList = "channelName"),
                @Index(columnList = "channelFlag"),
                @Index(name = "channel_flag_channel_name", columnList = "channelName"),
                @Index(name = "channel_flag_channel_name", columnList = "channelFlag"),
        })
public class RmiLogEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private Date createTime;//创建时间

    private String channelFlag;//通道标识

    private String channelName;//通道名称

    @Column(columnDefinition = "text")
    private String requestParameters;//请求参数

    @Column(columnDefinition = "text")
    private String result;//三方返回内容
}
