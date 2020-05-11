package com.pay.manager.pc.mcp.params;

import com.pay.common.enums.EncryptionType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class McpConfigReqParams {

    @NotNull(message = "请选择下游商户")
    private Long merchantId;

    @NotNull(message = "请选择上游通道")
    private Long channelId;

    @NotBlank(message = "上游下放商户号不能为空")
    private String upMerchantNo;

    @NotNull(message = "加密类型必须配置")
    private EncryptionType encryptionType;

    @NotBlank(message = "请配置密钥")
    private String upKey;

    @NotNull(message = "支付方式必须配置")
    private List<Long> mcpPayTypeIds;
}
