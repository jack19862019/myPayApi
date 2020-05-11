package com.pay.data.mapper;


import com.pay.common.enums.MenuLevel;
import com.pay.data.entity.SysMenuEntity;

import java.util.List;

/**
 * 菜单管理
 */
public interface SysMenuRepository extends BaseRepository<SysMenuEntity, Long> {

    List<SysMenuEntity> findByMenuLevel(MenuLevel menuLevel);

}
