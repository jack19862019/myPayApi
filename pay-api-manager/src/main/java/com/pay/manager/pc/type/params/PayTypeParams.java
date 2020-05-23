package com.pay.manager.pc.type.params;

import com.pay.data.entity.PayTypeEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@BeanCopySource(source = PayTypeEntity.class)
public class PayTypeParams {

    private Long id;

    @NotBlank(message = "支付方式名称不能为空")
    private String payTypeName;

    @NotBlank(message = "支付方式标识不能为空")
    private String payTypeFlag;

    private String remark;
}
