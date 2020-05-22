package com.pay.manager.pc.channel.params;

import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

@Data
@BeanCopySource(source = ChannelReqParams.class)
public class ChannelBeanOption {

    private String channelName;

    private String channelFlag;

    private String remark;
/*
    @CopyCollection(targetClass = UpPayTypeEntity.class, property = "channelPayTypeParams")
    private List<ChannelPayTypeBeanOption> upPayTypes = new ArrayList<>();*/
}
