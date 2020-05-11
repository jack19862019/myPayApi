package com.pay.manager.pc.user;

import com.pay.common.constant.Constant;
import com.pay.common.exception.Assert;
import com.pay.common.page.PageReqParams;
import com.pay.common.security.SecurityUtils;
import com.pay.data.entity.SysMenuEntity;
import com.pay.data.entity.SysRoleEntity;
import com.pay.data.entity.SysUserEntity;
import com.pay.data.mapper.SysRoleRepository;
import com.pay.data.mapper.SysUserRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.data.tree.MenuNode;
import com.pay.data.tree.TreeBuilder;
import com.pay.manager.pc.user.params.*;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;


@Service
public class SysUserServiceImpl extends AbstractHelper<SysUserRepository, SysUserEntity, Long> implements SysUserService {


    @Override
    public void insert(SysUserAddReqParams reqParams) {
        reqParams.setPassword(passwordEncoder.encode(reqParams.getPassword()));
        SysUserEntity sysUserEntity = BeanCopyUtils.copyBean(reqParams, SysUserEntity.class);
        assertedRole(sysUserEntity, reqParams.getRoleId());
        this.save(sysUserEntity);
    }

    private void assertedRole(SysUserEntity sysUserEntity, Long roleId) {
        Optional<SysRoleEntity> roleEntity = sysRoleRepository.findById(roleId);
        Assert.mustBeTrue(roleEntity.isPresent(), "未找到您关联的角色");
        sysUserEntity.setRole(roleEntity.get());
    }

    @Override
    public void delete(Long id) {
        deleteById(id);
    }

    @Override
    public void update(Long id, SysUserUpdateReqParams reqParams) {
        SysUserEntity sysUserEntity = getById(id);
        BeanCopyUtils.copyBean(reqParams, sysUserEntity);
        assertedRole(sysUserEntity, reqParams.getRoleId());
        save(sysUserEntity);
    }

    @Override
    public List<MenuNode> getUserMenus() {
        SysRoleEntity role = getById(SecurityUtils.getUserId()).getRole();
        Assert.isEmpty("该用户没有配置角色,请联系系统管理员！", role);
        List<SysMenuEntity> collect = role.getMenus().stream().sorted(Comparator.comparing(SysMenuEntity::getSort)).collect(toList());
        Assert.mustBeTrue(!CollectionUtils.isEmpty(collect), "用户角色未配置任何菜单,请联系系统管理员！");
        List<MenuNode> menuNodes = BeanCopyUtils.copyList(collect, MenuNode.class);
        return TreeBuilder.buildMenuTree(menuNodes);
    }

    @Override
    public Page<SysUserRespParams> getUsers(UserQuery criteria, PageReqParams reqParams) {
        criteria.setNotShow(SecurityUtils.getUsername());
        Page<SysUserEntity> list = getPage(criteria, toPageable(reqParams, Constant.CREATE_TIME));
        return list.map(e -> pageCopy(e, SysUserRespParams.class));
    }

    @Override
    public SysUserRespParams select(Long id) {
        return BeanCopyUtils.copyBean(getById(id), SysUserRespParams.class);
    }

    @Override
    public void updatePassword(SysUserPasswordParams passwordParams) {
        SysUserEntity byId = getById(SecurityUtils.getUserId());
        boolean matches = passwordEncoder.matches(passwordParams.getOldPassword(), byId.getPassword());
        Assert.mustBeTrue(matches, "旧密码不正确");
        byId.setPassword(passwordEncoder.encode(passwordParams.getNewPassword()));
        save(byId);
    }


    @Autowired
    SysRoleRepository sysRoleRepository;

    @Autowired
    SysUserRepository sysUserRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

}
