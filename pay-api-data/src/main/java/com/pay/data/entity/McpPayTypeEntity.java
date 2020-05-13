package com.pay.data.entity;

import com.pay.common.enums.AmountType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "mcp_pay_type")
public class McpPayTypeEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "pay_type_id")
    private PayTypeEntity payType;

    @ManyToOne
    @JoinColumn(name = "mcp_config_id")
    private McpConfigEntity mcpConfig;

    private String amountStr;

    private AmountType amountType;
}
