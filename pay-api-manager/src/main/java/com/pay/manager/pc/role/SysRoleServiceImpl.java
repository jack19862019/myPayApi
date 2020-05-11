package com.pay.manager.pc.role;


import com.pay.common.constant.Constant;
import com.pay.common.exception.Assert;
import com.pay.common.security.SecurityUtils;
import com.pay.data.entity.SysMenuEntity;
import com.pay.data.entity.SysRoleEntity;
import com.pay.data.entity.SysUserEntity;
import com.pay.data.mapper.SysMenuRepository;
import com.pay.data.mapper.SysRoleRepository;
import com.pay.data.mapper.SysUserRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.data.tree.MenuNode;
import com.pay.data.tree.TreeBuilder;
import com.pay.manager.pc.cache.CacheService;
import com.pay.manager.pc.role.params.*;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;


@Service
@Transactional
public class SysRoleServiceImpl extends AbstractHelper<SysRoleRepository, SysRoleEntity, Long> implements SysRoleService {


    @Override
    public void insert(SysRoleReqParams reqParams) {
        SysRoleEntity sysRoleEntity = BeanCopyUtils
                .copyBean(reqParams, SysRoleEntity.class, RoleBeanOption.class);
        save(sysRoleEntity);
    }

    @Override
    public void update(Long id, SysRoleReqParams reqParams) {
        SysRoleEntity roleById = getById(id);
        BeanCopyUtils.copyBean(reqParams, roleById, RoleBeanOption.class);
        save(roleById);
    }

    @Override
    public void delete(Long id) {
        //不能删除商户角色
        SysRoleEntity role = getById(id);
        Assert.mustBeTrue(!role.getNum().equals(Constant.JS002), "商户角色不允许删除");
        sysRoleRepository.delete(role);
    }

    @Override
    public SysRoleRespParams select(Long id) {
        SysRoleRespParams sysRoleRespParams = BeanCopyUtils.copyBean(getById(id), SysRoleRespParams.class);
        sysRoleRespParams.setMenuNodes(new HashSet<>(selectRoleMenus(id)));
        return sysRoleRespParams;
    }

    private List<MenuNode> selectRoleMenus(Long id) {
        //角色已经关联的菜单
        List<SysMenuEntity> list = new ArrayList<>(getById(id).getMenus());
        //所有菜单
        SysUserEntity sysUserEntity = sysUserRepository.getOne(SecurityUtils.getUserId());
        Set<SysMenuEntity> allMenus = sysUserEntity.getRole().getMenus();
        if (!CollectionUtils.isEmpty(sysUserEntity.getRole().getMenus())) {
            allMenus = allMenus.stream().filter(e -> !e.getName().equals("菜单管理")).collect(toSet());
            //对比回显设置true
            List<MenuNode> menuNodes = BeanCopyUtils.copyList(new ArrayList<>(allMenus), MenuNode.class);
            menuNodes.stream()
                    .filter(e -> list.stream().map(SysMenuEntity::getId).collect(toList()).contains(e.getId()))
                    .forEach(b -> b.setIsDisplay(true));
            return TreeBuilder.buildMenuTree(menuNodes);
        }
        return new ArrayList<>();
    }

    @Override
    public List<SysRoleRespParams> selectList(RoleQuery roleQuery) {
        List<SysRoleEntity> list = getList(roleQuery);
        //list = list.stream().filter(e -> !e.getNum().equals(Constant.JS001)).collect(toList());
        return BeanCopyUtils.copyList(list, SysRoleRespParams.class);
    }

    @Override
    public void associatedMenus(Long id, List<Long> menuIds) {
        SysRoleEntity roleById = getById(id);
        List<SysMenuEntity> menus = sysMenuRepository.findAllById(menuIds);
        roleById.setMenus(new HashSet<>(menus));
        save(roleById);
    }

    @Autowired
    SysRoleRepository sysRoleRepository;

    @Autowired
    SysUserRepository sysUserRepository;

    @Autowired
    SysMenuRepository sysMenuRepository;

    @Autowired
    CacheService cacheService;
}
