package com.pay.data.entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "mcp_pay_type")
public class McpPayTypeEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "up_pay_type_id")
    private UpPayTypeEntity upPayType;

    @ManyToOne(cascade = CascadeType.REFRESH)
    private McpConfigEntity mcpConfig;

}
