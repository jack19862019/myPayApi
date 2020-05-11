package com.pay.manager.pc.merchant.params;

import com.pay.data.entity.MerchantEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Data;

@Data
@BeanCopySource(source = MerchantEntity.class)
public class MerchantDetailRespParams {

    private String merchantNo;

    private String merchantName;

    private String nickName;

    private String remark;

    private String md5Key;

}
