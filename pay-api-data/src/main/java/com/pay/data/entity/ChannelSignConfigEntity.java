package com.pay.data.entity;

import com.pay.common.enums.IsCustom;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "channel_sign_params")
public class ChannelSignConfigEntity extends BaseEntity {

    private String chineseStr;

    private String upReqStr;

    private Integer sort;

    private String upReqJoinTypeStr;//参数链接方式

    private String keyJoinTypeStr;//密钥链接方式

    private IsCustom isCustom;//是否自定义

    private String customScript;//自定义脚本

    @ManyToOne
    private ChannelEntity channel;

}
