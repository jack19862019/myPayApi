package com.pay.manager.pc.mcp.params;

import com.pay.data.entity.McpPayTypeEntity;
import com.pay.manager.pc.upPayType.params.UpPayTypeRespParams;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Data;

@Data
@BeanCopySource(source = McpPayTypeEntity.class)
public class McpPayTypeParams {

    @CopyProperty(property = "upPayType")
    private UpPayTypeRespParams upPayTypeParams;
}
