package com.pay.manager.pc.channel.params;

import com.pay.data.entity.ChannelEntity;
import com.pay.manager.pc.contact.ContactParams;
import com.pay.manager.pc.type.params.PayTypeParams;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyCollection;
import lombok.Data;

import java.util.List;

@Data
@BeanCopySource(source = ChannelEntity.class)
public class ChannelDetailRespParams {

    private Long id;

    private String channelFlag;

    private String channelName;

    private String remark;

    @CopyCollection(targetClass = PayTypeParams.class, property = "payTypes")
    private List<PayTypeParams> channelPayTypeParams;
}
