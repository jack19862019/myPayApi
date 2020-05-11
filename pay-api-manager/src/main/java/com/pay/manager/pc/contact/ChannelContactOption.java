package com.pay.manager.pc.contact;


import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

@Data
@BeanCopySource(source = ContactParams.class)
public class ChannelContactOption {

    //联系方式key
    private String contactKey;

    //联系方式值
    private String contactValue;

}
