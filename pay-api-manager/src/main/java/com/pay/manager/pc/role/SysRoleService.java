package com.pay.manager.pc.role;


import com.pay.manager.pc.role.params.RoleQuery;
import com.pay.manager.pc.role.params.SysRoleReqParams;
import com.pay.manager.pc.role.params.SysRoleRespParams;

import java.util.List;

public interface SysRoleService {

    void insert(SysRoleReqParams reqParams);

    void update(Long id, SysRoleReqParams reqParams);

    void delete(Long id);

    List<SysRoleRespParams> selectList(RoleQuery roleQuery);

    SysRoleRespParams select(Long id);

    void associatedMenus(Long id, List<Long> menuIds);
}
