package com.pay.manager.pc.upPayType.params;

import com.pay.data.entity.PayTypeEntity;
import com.pay.data.entity.UpPayTypeEntity;
import com.pay.manager.pc.type.params.PayTypeParams;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Data;

@Data
@BeanCopySource(source = UpPayTypeEntity.class)
public class UpPayTypeRespParams {

    //支付方式名称
    private String upPayTypeName;

    //支付方式 标识
    private String upPayTypeFlag;

    //备注
    private String remark;

    @CopyProperty(property = "payType")
    private PayTypeParams payTypeParams;
}
