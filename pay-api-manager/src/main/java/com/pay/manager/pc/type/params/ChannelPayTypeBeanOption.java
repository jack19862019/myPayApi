package com.pay.manager.pc.type.params;

import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

@Data
@BeanCopySource(source = PayTypeParams.class)
public class ChannelPayTypeBeanOption {

    private Long id;
}
