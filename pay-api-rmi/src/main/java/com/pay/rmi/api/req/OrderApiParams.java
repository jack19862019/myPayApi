package com.pay.rmi.api.req;

import com.pay.common.annotation.EnumValidAnnotation;
import com.pay.common.enums.EncryptionType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OrderApiParams {

    @NotBlank(message = "签名不能为空")
    private String sign;

    @NotNull(message = "加密报文不能为空")
    private byte[] context;

    @EnumValidAnnotation(message = "加密方式错误",target = EncryptionType.class )
    private EncryptionType encryptType;
}
