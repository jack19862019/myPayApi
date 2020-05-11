package com.pay.data.mapper;

import com.pay.data.entity.SysUserEntity;

import java.util.Set;

/**
 * 系统用户
 */
public interface SysUserRepository extends BaseRepository<SysUserEntity, Long> {

    SysUserEntity findByUsername(String username);

    Set<SysUserEntity> findByRole_Id(Long roleId);
}
