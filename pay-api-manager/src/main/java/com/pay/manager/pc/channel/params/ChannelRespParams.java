package com.pay.manager.pc.channel.params;

import com.pay.data.entity.ChannelEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@BeanCopySource(source = ChannelEntity.class)
public class ChannelRespParams {

    private String channelName;

    private String channelFlag;

    private String remark;
}
