package com.pay.manager.pc.user;


import com.pay.common.page.PageReqParams;
import com.pay.data.tree.MenuNode;
import com.pay.manager.pc.user.params.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SysUserService {

    void insert(SysUserAddReqParams sysUserAddReqParams);

    void delete(Long id);

    void update(Long id, SysUserUpdateReqParams reqParams);

    List<MenuNode> getUserMenus();

    Page<SysUserRespParams> getUsers(UserQuery userQuery, PageReqParams reqParams);

    SysUserRespParams select(Long id);

    void updatePassword(SysUserPasswordParams passwordParams);
}
