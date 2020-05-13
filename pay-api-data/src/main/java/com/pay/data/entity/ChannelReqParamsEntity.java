package com.pay.data.entity;

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
@Table(name = "up_req_params",
        indexes = {
                @Index(columnList = "upReqParamsKey"),
                @Index(columnList = "valuePropertyParams"),
        })
public class ChannelReqParamsEntity extends BaseEntity {

    private String upReqParamsKey;

    private String valuePropertyParams;

}
