package com.pay.manager.pc.mcp.params;

import com.pay.common.enums.EncryptionType;
import com.pay.data.entity.McpConfigEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyCollection;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Data;

import java.util.List;

@Data
@BeanCopySource(source = McpConfigEntity.class)
public class McpConfigDetailParams {

    private Long id;

    @CopyProperty(property = "merchant")
    private McpMerchantParams merchantParams;

    @CopyProperty(property = "channel")
    private McpChannelParams channelParams;

    private String upMerchantNo;

    private EncryptionType encryptionType;

    private String upKey;
}
