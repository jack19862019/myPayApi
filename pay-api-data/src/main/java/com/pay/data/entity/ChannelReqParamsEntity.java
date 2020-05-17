package com.pay.data.entity;

import com.pay.common.enums.IsDelete;
import com.pay.common.enums.IsValue;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "channel_up_req_params")
public class ChannelReqParamsEntity extends BaseEntity {

    private String chineseStr;

    private String downReqStr;

    private String upReqStr;

    private IsValue isValue;

    private String pattenStr;

    @ManyToOne
    private ChannelEntity channel;

}
