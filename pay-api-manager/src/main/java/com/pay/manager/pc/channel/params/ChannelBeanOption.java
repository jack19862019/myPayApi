package com.pay.manager.pc.channel.params;

import com.pay.data.entity.PayTypeEntity;
import com.pay.manager.pc.type.params.ChannelPayTypeBeanOption;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyCollection;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@BeanCopySource(source = ChannelReqParams.class)
public class ChannelBeanOption {

    private String channelName;

    private String channelFlag;

    private String remark;

    @CopyCollection(targetClass = PayTypeEntity.class, property = "channelPayTypeParams")
    private List<ChannelPayTypeBeanOption> payTypes = new ArrayList<>();
}
