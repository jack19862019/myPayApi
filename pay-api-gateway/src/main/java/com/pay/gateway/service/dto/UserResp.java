package com.pay.gateway.service.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.pay.data.entity.SysUserEntity;
import com.pay.common.enums.IsDelete;
import com.pay.gateway.auth.resp.Role;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;


@Setter
@Getter
@BeanCopySource(source = SysUserEntity.class)
public class UserResp implements Serializable {

    private Long id;

    private String username;

    @JSONField(serialize = false)
    private String password;

    private String email;

    private String phone;

    private IsDelete isDelete;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Role role;
}
