package com.pay.data.entity;

import com.pay.common.enums.IsValue;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "channel_up_req_params")
public class ChannelReqMapEntity extends BaseEntity {

    private String chineseStr;

    private String downReqStr;

    private String upReqStr;

    private IsValue isValue;

    private String pattenStr;

    /*private IsSign isSign;

    private Integer sort;

    private String joinTypeStr;*/

    @ManyToOne
    private ChannelEntity channel;

}
