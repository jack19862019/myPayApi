package com.pay.manager.pc.mcp.params;

import com.pay.data.entity.MerchantEntity;
import com.pay.manager.pc.user.params.SysUserRespParams;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Data;

@Data
@BeanCopySource(source = MerchantEntity.class)
public class McpMerchantParams {

    private Long id;

    //商户号
    private String merchantNo;

    //商户名称
    private String merchantName;

    //商户别名，例K1，K2
    private String nickName;

    //商户上报密钥
    private String md5Key;

    //备注
    private String remark;

    @CopyProperty(property = "user")
    private SysUserRespParams userRespParams;
}
