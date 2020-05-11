package com.pay.manager.pc.config;

import com.pay.common.exception.Assert;
import com.pay.data.entity.SysConfigEntity;
import com.pay.data.mapper.SysConfigRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.config.params.SysConfigPrams;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class SysConfigServiceImpl extends AbstractHelper<SysConfigRepository, SysConfigEntity, Long> implements SysConfigService {

    @Override
    public void insertConfig(SysConfigPrams sysConfigPrams) {
        SysConfigPrams configs = getConfig();
        Assert.mustBeTrue(ObjectUtils.isEmpty(configs), "已经存在有效的系统配置项，不允许再添加");
        save(BeanCopyUtils.copyBean(sysConfigPrams, SysConfigEntity.class));
    }

    @Override
    public void deleteConfig(Long id) {
        deleteById(id);
    }

    @Override
    public SysConfigPrams getConfig() {
        List<SysConfigEntity> list = getList(null);
        Assert.mustBeTrue(CollectionUtils.isEmpty(list) || list.size() < 2, "系统配置数据异常");
        return BeanCopyUtils.copyBean(list.get(0), SysConfigPrams.class);
    }

    @Override
    public void updateConfig(Long id, SysConfigPrams sysConfigPrams) {
        save(BeanCopyUtils.copyBean(getById(id), SysConfigEntity.class));
    }
}
