package com.pay.manager.pc.mcp.params;

import com.pay.common.enums.EncryptionType;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

@Data
@BeanCopySource(source = McpConfigReqParams.class)
public class McpConfigOption {

    private String upMerchantNo;

    private EncryptionType encryptionType;

    private String upKey;
}
