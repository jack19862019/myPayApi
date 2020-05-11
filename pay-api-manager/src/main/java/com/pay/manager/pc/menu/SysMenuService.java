package com.pay.manager.pc.menu;

import com.pay.common.enums.MenuLevel;
import com.pay.data.tree.MenuNode;
import com.pay.manager.pc.menu.params.MenuQuery;
import com.pay.manager.pc.menu.params.SysMenuReqParams;
import com.pay.manager.pc.menu.params.SysMenuResParams;

import java.util.List;

public interface SysMenuService {


    void insert(SysMenuReqParams reqParams);

    void update(Long id, SysMenuReqParams reqParams);

    void delete(Long id);

    List<MenuNode> selectPage(MenuQuery menuQuery);

    SysMenuResParams select(Long id);

    List<MenuNode> selectParentMenu(MenuLevel menuLevel);
}
