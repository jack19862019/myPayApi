package com.pay.gateway.runner;

import com.pay.common.constant.Constant;
import com.pay.data.entity.SysMenuEntity;
import com.pay.data.entity.SysRoleEntity;
import com.pay.data.entity.SysUserEntity;
import com.pay.common.enums.MenuLevel;
import com.pay.data.mapper.SysMenuRepository;
import com.pay.data.mapper.SysRoleRepository;
import com.pay.data.mapper.SysUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
@Slf4j
@Order(1)
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //初始化数据库数据
        sysUserRepository = contextRefreshedEvent.getApplicationContext().getBean(SysUserRepository.class);
        sysRoleRepository = contextRefreshedEvent.getApplicationContext().getBean(SysRoleRepository.class);
        sysMenuRepository = contextRefreshedEvent.getApplicationContext().getBean(SysMenuRepository.class);
        passwordEncoder = contextRefreshedEvent.getApplicationContext().getBean(BCryptPasswordEncoder.class);

        SysUserEntity byUsername = sysUserRepository.findByUsername(DEFAULT_USERNAME);
        if (ObjectUtils.isEmpty(byUsername)) {
            this.initMenu().initRole().initUser().create();
            //区分完层级结构（后端需要做得时区）后，前端需要path
            List<SysMenuEntity> allMenus = sysMenuRepository.findAll();
            allMenus.forEach(e->{
                e.setPath(e.getAlias());
                if (ObjectUtils.isEmpty(e.getParent())){
                    e.setParent(e);
                }
            });
            sysMenuRepository.saveAll(allMenus);
        }
    }

    private ApplicationStartup initUser() {
        SysUserEntity sysUser = new SysUserEntity();
        sysUser.setPhone("13234567890");
        sysUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        sysUser.setEmail("1234@qq.com");
        sysUser.setUsername(DEFAULT_USERNAME);
        sysUser.setRole(sysRoleEntities.stream().filter(e->e.getNum().equals(Constant.JS001)).findFirst().get());
        sysUserRepository.save(sysUser);
        return this;
    }

    private ApplicationStartup initMenu() {
        Set<SysMenuEntity> sysMenuEntities = new HashSet<>();
        SysMenuEntity sysMenu0 = new SysMenuEntity("系统管理", 0, "admin/sys", "sys", "sys", MenuLevel.TITLE);
        SysMenuEntity sysMenu1 = new SysMenuEntity("角色管理", 1, "sys/role", "role", "role", MenuLevel.MENU);
        SysMenuEntity sysMenu2 = new SysMenuEntity("用户管理", 2, "sys/user", "user", "user", MenuLevel.MENU);
        SysMenuEntity sysMenu4 = new SysMenuEntity("菜单管理", 4, "sys/menu", "menu", "menu", MenuLevel.MENU);

        SysMenuEntity sysMenu8 = new SysMenuEntity("系统监控", 0, "admin/monitoring", "monitoring", "monitoring", MenuLevel.TITLE);
        SysMenuEntity sysMenu9 = new SysMenuEntity("操作日志", 1, "monitoring/log", "log", "log", MenuLevel.MENU);
        SysMenuEntity sysMenu10 = new SysMenuEntity("异常日志", 2, "monitoring/error", "error", "error", MenuLevel.MENU);
        SysMenuEntity sysMenu11 = new SysMenuEntity("系统缓存", 3, "monitoring/redis", "redis", "redis", MenuLevel.MENU);
        SysMenuEntity sysMenu12 = new SysMenuEntity("SQL监控", 4, "monitoring/sql", "sql", "sql", MenuLevel.MENU);

        SysMenuEntity sysMenu13 = new SysMenuEntity("支付管理", 0, "pay/manager", "manager", "manager", MenuLevel.TITLE);
        SysMenuEntity sysMenu14 = new SysMenuEntity("商户管理", 1, "manager/merchant", "merchant", "merchant", MenuLevel.MENU);
        SysMenuEntity sysMenu15 = new SysMenuEntity("通道管理", 2, "manager/channel", "channel", "channel", MenuLevel.MENU);

        sysMenuEntities.add(sysMenu13);
        sysMenuEntities.add(sysMenu14);
        sysMenuEntities.add(sysMenu15);

        sysMenuEntities.add(sysMenu0);
        sysMenuEntities.add(sysMenu1);
        sysMenuEntities.add(sysMenu2);
        sysMenuEntities.add(sysMenu4);
        sysMenuEntities.add(sysMenu8);
        sysMenuEntities.add(sysMenu9);
        sysMenuEntities.add(sysMenu10);
        sysMenuEntities.add(sysMenu11);
        sysMenuEntities.add(sysMenu12);

        sysMenuEntities.stream()
                .filter(e -> e.getPath().startsWith("sys"))
                .forEach(e -> e.setParent(sysMenu0));

        sysMenuEntities.stream()
                .filter(e -> e.getPath().startsWith("monitoring"))
                .forEach(e -> e.setParent(sysMenu8));
        Set<SysMenuEntity> bs = new HashSet<>();
        sysMenuEntities.stream()
                .filter(e -> e.getMenuLevel().equals(MenuLevel.MENU))
                .forEach(e -> {
                    if (e.getPath().contains("sys")) {
                        e.setParent(sysMenu0);
                        String[] buttons = new String[]{"新增:add", "修改:update", "删除:delete", "搜索:select"};
                        Set<SysMenuEntity> sysMenuEntities1 = addButton(e, buttons);
                        bs.addAll(sysMenuEntities1);
                    }
                    if (e.getPath().contains("manager")) {
                        e.setParent(sysMenu13);
                        String[] buttons = new String[]{"新增:add", "修改:update", "删除:delete", "搜索:select"};
                        Set<SysMenuEntity> sysMenuEntities1 = addButton(e, buttons);
                        bs.addAll(sysMenuEntities1);
                    }
                    if (e.getPath().contains("monitoring")) {
                        e.setParent(sysMenu8);
                        String[] buttons = new String[]{"搜索:select"};
                        Set<SysMenuEntity> sysMenuEntities1 = addButton(e, buttons);
                        bs.addAll(sysMenuEntities1);
                    }
                });
        sysMenuEntities.addAll(bs);
        this.sysMenuEntities = sysMenuRepository.saveAll(sysMenuEntities);
        return this;
    }

    private Set<SysMenuEntity> addButton(SysMenuEntity e, String[] buttons) {
        Set<SysMenuEntity> sysMenuEntities = new HashSet<>();
        for (int i = 0; i < buttons.length; i++) {
            String button = buttons[i];
            String name = button.split(":")[0];
            String nik = button.split(":")[1];
            SysMenuEntity sysMenuEntity =
                    new SysMenuEntity(
                            name,
                            i,
                            e.getAlias()+"/"+nik,
                            e.getAlias()+ StringUtils.capitalize(nik),
                            e.getAlias()+ StringUtils.capitalize(nik),
                            MenuLevel.BUTTON
                    );
            sysMenuEntity.setParent(e);
            sysMenuEntities.add(sysMenuEntity);
        }
        return sysMenuEntities;
    }

    private ApplicationStartup initRole() {
        SysRoleEntity sysRole = new SysRoleEntity();
        sysRole.setName("超级管理员");
        sysRole.setRemark("所有权限");
        sysRole.setNum(Constant.JS001);
        sysRole.setMenus(new HashSet<>(sysMenuEntities));

        SysRoleEntity sysRole1 = new SysRoleEntity();
        sysRole1.setName("商户角色");
        sysRole1.setRemark("商户菜单");
        sysRole1.setNum(Constant.JS002);
        sysRole1.setMenus(sysMenuEntities.stream().filter(e->e.getPath().contains("manager")).collect(Collectors.toSet()));

        List<SysRoleEntity> list = new ArrayList<>();
        list.add(sysRole);list.add(sysRole1);
        sysRoleEntities = sysRoleRepository.saveAll(list);
        return this;
    }

    private void create() {
    }

    private static final String DEFAULT_USERNAME = "admin";

    private static final String DEFAULT_PASSWORD = "123456";

    private List<SysMenuEntity> sysMenuEntities;

    private List<SysRoleEntity> sysRoleEntities;

    private SysRoleRepository sysRoleRepository;

    private SysMenuRepository sysMenuRepository;

    private SysUserRepository sysUserRepository;

    private BCryptPasswordEncoder passwordEncoder;
}
