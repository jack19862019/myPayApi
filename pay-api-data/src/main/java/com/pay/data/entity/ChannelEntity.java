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
@Table(name = "channel",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"channelName", "channelFlag"})},
        indexes = {
                @Index(columnList = "channelName"),
                @Index(columnList = "channelFlag"),
        })
public class ChannelEntity extends BaseEntity {

    //支付通道名称
    private String channelName;

    //支付通道标识（名称的拼音首字母）
    private String channelFlag;

    private String upPayUrl;

    //备注
    private String remark;

    //联系人
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "channel_id")
    private Set<ChannelContactEntity> contacts = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "channelFlag", referencedColumnName = "channelFlag")
    private Set<UpPayTypeEntity> upPayTypes = new HashSet<>();

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "channel")
    private Set<McpConfigEntity> mcpConfigs = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "channel")
    private Set<OrderEntity> orders = new HashSet<>();


}
