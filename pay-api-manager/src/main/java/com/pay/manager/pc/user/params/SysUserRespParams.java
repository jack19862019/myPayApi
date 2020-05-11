package com.pay.manager.pc.user.params;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pay.data.entity.SysUserEntity;
import com.pay.manager.pc.merchant.params.MerchantDetailRespParams;
import com.pay.manager.pc.merchant.params.MerchantPageRespParams;
import com.pay.manager.pc.role.params.SysUserRolesResParams;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Data;

import java.util.Date;

@Data
@BeanCopySource(source = SysUserEntity.class)
public class SysUserRespParams {

    private Long id;

    private String username;

    private String email;

    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private String createUser;

    private SysUserRolesResParams role;

    @CopyProperty(property = "merchant")
    private MerchantPageRespParams merchantRespParams;
}
