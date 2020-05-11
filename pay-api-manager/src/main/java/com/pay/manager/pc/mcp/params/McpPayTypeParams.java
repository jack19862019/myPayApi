package com.pay.manager.pc.mcp.params;

import com.pay.common.enums.AmountType;
import com.pay.data.entity.McpPayTypeEntity;
import com.pay.data.entity.PayTypeEntity;
import com.pay.manager.pc.type.params.PayTypeParams;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@BeanCopySource(source = McpPayTypeEntity.class)
public class McpPayTypeParams {

    private Long id;

    @CopyProperty(property = "payType")
    private PayTypeParams payTypeParams;

    private String amountStr;

    private AmountType amountType;
}
