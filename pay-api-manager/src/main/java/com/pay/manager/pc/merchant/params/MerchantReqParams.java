package com.pay.manager.pc.merchant.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MerchantReqParams {

    @NotBlank(message = "商户编号不能为空")
    private String merchantNo;

    @NotBlank(message = "商户名称不能为空")
    private String merchantName;

    @NotBlank(message = "商户所属归类不能为空")
    private String nickName;

    private String remark;
}
