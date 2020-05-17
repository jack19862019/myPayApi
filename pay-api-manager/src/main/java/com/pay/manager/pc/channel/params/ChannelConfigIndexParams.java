package com.pay.manager.pc.channel.params;

import com.pay.common.enums.IsValue;
import com.pay.data.entity.ChannelReqParamsEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

@Data
@BeanCopySource(source = ChannelReqParamsEntity.class)
public class ChannelConfigIndexParams {

    private String chineseStr;

    private String downReqStr;

    private String upReqStr;

    private IsValue isValue;

    private String pattenStr;
}
