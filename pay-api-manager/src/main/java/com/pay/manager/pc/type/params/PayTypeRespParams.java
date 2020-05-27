package com.pay.manager.pc.type.params;

import com.pay.data.entity.PayTypeEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

@Data
@BeanCopySource(source = PayTypeEntity.class)
public class PayTypeRespParams {

    private Long id;

    //支付方式名称
    private String payTypeName;

    //支付方式 标识
    private String payTypeFlag;

    //备注
    private String remark;
}
