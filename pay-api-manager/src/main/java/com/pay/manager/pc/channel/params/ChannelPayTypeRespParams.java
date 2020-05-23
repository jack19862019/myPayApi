package com.pay.manager.pc.channel.params;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.PayTypeEntity;
import com.pay.data.entity.UpPayTypeEntity;
import com.pay.manager.pc.type.params.PayTypeParams;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@BeanCopySource(source = UpPayTypeEntity.class)
public class ChannelPayTypeRespParams {

    private String upPayTypeName;

    private String upPayTypeFlag;

    private PayTypeParams payType;
}
