package com.pay.manager.pc.channel.params;

import com.pay.common.enums.IsValue;
import com.pay.data.entity.ChannelReqMapEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

@Data
@BeanCopySource(source = ChannelReqMapEntity.class)
public class ChannelReqIndexParams {

    private String chineseStr;

    private String downReqStr;

    private String upReqStr;

    private IsValue isValue;

    private String pattenStr;
}
