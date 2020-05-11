package com.pay.manager.pc.statistics.params;

import com.pay.common.annotation.EnumValidAnnotation;
import com.pay.common.enums.DateIntervalType;
import com.pay.common.enums.MerChaType;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class IndexReqParams {

    private DateIntervalType dateIntervalType;

    private String dateScope;

    @NotBlank(message = "【商户标识】或【通道标识】不能为空")
    private String mocFlag;

    @EnumValidAnnotation(message = "【商户ID】或【通道ID】不能为空")
    private MerChaType merChaType;
}
