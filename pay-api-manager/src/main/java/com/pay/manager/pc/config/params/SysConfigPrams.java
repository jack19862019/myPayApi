package com.pay.manager.pc.config.params;

import com.pay.common.enums.IsDelete;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SysConfigPrams {

    @ApiModelProperty(hidden = true)
    private Long id;

    @ApiModelProperty(hidden = true)
    private IsDelete isDelete;

    @NotBlank(message = "此参数涉及回调域名，必须添加")
    private String domain;

    @NotBlank(message = "此参数为商户初始密码，必须添加")
    private String initPassword;
}
