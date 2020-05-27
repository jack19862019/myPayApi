package com.pay.manager.pc.type.params;

import com.pay.data.entity.UpPayTypeEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Data;

@Data
@BeanCopySource(source = UpPayTypeEntity.class)
public class PayTypeParams {

    private Long id;

    private String upPayTypeName;

    private String upPayTypeFlag;

    @CopyProperty(property = "payType")
    private PayTypeRespParams payTypeRespParams;
}
