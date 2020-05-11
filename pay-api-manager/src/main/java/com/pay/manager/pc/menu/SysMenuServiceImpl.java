package com.pay.manager.pc.menu;

import com.pay.common.constant.Constant;
import com.pay.common.enums.MenuLevel;
import com.pay.common.exception.Assert;
import com.pay.common.security.SecurityUtils;
import com.pay.data.entity.SysMenuEntity;
import com.pay.data.entity.SysRoleEntity;
import com.pay.data.mapper.SysMenuRepository;
import com.pay.data.mapper.SysRoleRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.data.tree.MenuNode;
import com.pay.data.tree.TreeBuilder;
import com.pay.manager.pc.menu.params.MenuQuery;
import com.pay.manager.pc.menu.params.SysMenuReqParams;
import com.pay.manager.pc.menu.params.SysMenuResParams;
import com.pay.manager.pc.role.SysRoleService;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;


@Service
public class SysMenuServiceImpl extends AbstractHelper<SysMenuRepository, SysMenuEntity, Long> implements SysMenuService {


    @Override
    public void insert(SysMenuReqParams reqParams) {
        this.checkAdminLogin();
        Long parentId = reqParams.getPid();
        SysMenuEntity sysMenuEntity = BeanCopyUtils.copyBean(reqParams, SysMenuEntity.class);
        sysMenuEntity.setParent(parentId == null ? sysMenuEntity : getById(parentId));
        SysMenuEntity saveMenu = save(sysMenuEntity);
        //自动关联admin
        associatedToAdmin(saveMenu);
    }

    @Override
    public void update(Long id, SysMenuReqParams reqParams) {
        this.checkAdminLogin();
        SysMenuEntity sysMenuEntity = getById(id);
        BeanCopyUtils.copyBean(reqParams, sysMenuEntity);
        Long parentId = reqParams.getPid();
        sysMenuEntity.setParent(parentId == 0L ? null : getById(parentId));
        SysMenuEntity saveMenu = save(sysMenuEntity);
        //自动关联admin
        associatedToAdmin(saveMenu);
    }

    @Override
    public void delete(Long id) {
        this.checkAdminLogin();
        this.checkChildren(id);
        deleteById(id);
    }

    private void checkChildren(Long id) {
        SysMenuEntity sysMenu = getById(id);
        if (Arrays.asList(MenuLevel.BUTTON, MenuLevel.MENU).contains(sysMenu.getMenuLevel())){
            Assert.mustBeTrue(!sysMenu.getParent().getId().equals(sysMenu.getId()), "该菜单还存在子菜单，不允许删除");
        }
        if (MenuLevel.TITLE.equals(sysMenu.getMenuLevel())){
            Assert.mustBeTrue(sysMenu.getParent().getId().equals(sysMenu.getId()), "该菜单还存在子菜单，不允许删除");

        }
    }

    @Override
    public List<MenuNode> selectPage(MenuQuery menuQuery) {
        List<SysMenuEntity> list = getList(menuQuery, new Sort(Sort.Direction.ASC, "sort"));
        List<MenuNode> menuNodes = BeanCopyUtils.copyList(list, MenuNode.class);
        return TreeBuilder.buildMenuTree(menuNodes);
    }

    @Override
    public SysMenuResParams select(Long id) {
        return BeanCopyUtils.copyBean(getById(id), SysMenuResParams.class);
    }

    @Override
    public List<MenuNode> selectParentMenu(MenuLevel menuLevel) {
        menuLevel = MenuLevel.getStatusByCode(menuLevel.getCode() - 1);
        List<SysMenuEntity> menuEntities = sysMenuRepository.findByMenuLevel(menuLevel);
        return BeanCopyUtils.copyList(new ArrayList<>(menuEntities), MenuNode.class);
    }

    /**
     * 操作菜单只能是管理员
     */
    private void checkAdminLogin() {
        String username = SecurityUtils.getUsername();
        Assert.mustBeTrue(Constant.ADMIN.equals(username), "只有管理员可以操作菜单");
    }

    private void associatedToAdmin(SysMenuEntity sysMenuEntity) {
        Long userId = SecurityUtils.getUserId();
        SysRoleEntity role = sysRoleRepository.findByUsers_Id(userId);
        List<Long> menus = role.getMenus().stream().map(SysMenuEntity::getId).collect(toList());
        Long roleId = role.getId();
        menus.add(sysMenuEntity.getId());
        sysRoleService.associatedMenus(roleId, menus);
    }

    @Autowired
    SysMenuRepository sysMenuRepository;

    @Autowired
    SysRoleRepository sysRoleRepository;

    @Autowired
    SysRoleService sysRoleService;

}
