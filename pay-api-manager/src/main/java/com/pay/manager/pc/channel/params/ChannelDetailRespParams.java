package com.pay.manager.pc.channel.params;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.UpPayTypeEntity;
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

    private String upPayUrl;

    private String remark;

    @CopyCollection(targetClass = ChannelPayTypeRespParams.class, property = "upPayTypes")
    private List<ChannelPayTypeRespParams> upPayTypes;
}
