package com.pay.data.entity;

import com.pay.common.enums.AuditStatus;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Where(clause = "is_delete=1")
@Table(name = "merchant",
        uniqueConstraints = {@UniqueConstraint(columnNames={"merchantNo", "merchantName"})},
        indexes = {
                @Index(columnList = "merchantNo"),
                @Index(columnList = "merchantName"),
                @Index(columnList = "nickName"),
                @Index(columnList = "auditStatus"),
        })
public class MerchantEntity extends BaseEntity {

    //商户号
    private String merchantNo;

    //商户名称
    private String merchantName;

    //商户别名，例K1，K2
    private String nickName;

    //商户上报密钥
    @Column
    private String md5Key;

    //备注
    private String remark;

    @Convert(converter = AuditStatus.Convert.class)
    private AuditStatus auditStatus = AuditStatus.AUDIT_WAIT;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private SysUserEntity user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "merchant")
    private Set<McpConfigEntity> mcpConfigs;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "merchant")
    private Set<OrderEntity> orders;
}
