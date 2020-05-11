package com.pay.data.mapper;

import com.pay.data.entity.SysRoleEntity;
import com.pay.data.entity.SysUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Set;



public interface SysRoleRepository extends BaseRepository<SysRoleEntity, Long> {

    SysRoleEntity findByUsers_Id(Long usersId);

    SysRoleEntity findByNum(String num);

}
