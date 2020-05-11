package com.pay.manager.pc.merchant.params;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pay.data.entity.MerchantEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Data;

import java.util.Date;

@Data
@BeanCopySource(source = MerchantEntity.class)
public class MerchantPageRespParams {

    private Long id;

    private String md5Key;

    private String merchantNo;

    private String merchantName;

    private String nickName;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private String createUser;

    @CopyProperty(property = "user.username")
    private String username;

}
