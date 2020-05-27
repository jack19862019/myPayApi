package com.pay.manager.pc.merchant;

import com.pay.common.constant.Constant;
import com.pay.common.exception.Assert;
import com.pay.common.page.PageReqParams;
import com.pay.common.security.SecurityUtils;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.SysConfigEntity;
import com.pay.data.entity.SysRoleEntity;
import com.pay.data.entity.SysUserEntity;
import com.pay.data.mapper.MerchantRepository;
import com.pay.data.mapper.SysConfigRepository;
import com.pay.data.mapper.SysRoleRepository;
import com.pay.data.mapper.SysUserRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.cache.CacheService;
import com.pay.manager.pc.merchant.params.MerchantDetailRespParams;
import com.pay.manager.pc.merchant.params.MerchantPageRespParams;
import com.pay.manager.pc.merchant.params.MerchantQuery;
import com.pay.manager.pc.merchant.params.MerchantReqParams;
import com.pay.manager.pc.user.SysUserService;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Service
public class MerchantServiceImpl extends AbstractHelper<MerchantRepository, MerchantEntity, Long> implements MerchantService {

    @Override
    public void insert(MerchantReqParams reqParams) {
        SysRoleEntity sysRoleEntity = checkIsExistMerchantRole();
        //生成对下游的MD5密钥
        MerchantEntity merchantEntity = BeanCopyUtils.copyBean(reqParams, MerchantEntity.class);
        merchantEntity.setMd5Key(UUID.randomUUID().toString().replace("-", ""));
        //同时新增一个用户并且绑定到商户角色
        automaticInsertUserAndRoleMerchant(merchantEntity, sysRoleEntity);
    }

    private SysRoleEntity checkIsExistMerchantRole() {
        SysRoleEntity byNum = sysRoleRepository.findByNum(Constant.JS002);
        Assert.isEmpty("没有商户角色存在,请先新增商户角色", byNum);
        return byNum;
    }

    private void automaticInsertUserAndRoleMerchant(MerchantEntity merchant, SysRoleEntity sysRole) {
        SysUserEntity userEntity = new SysUserEntity();
        userEntity.setUsername(merchant.getMerchantNo());

        List<SysConfigEntity> all = sysConfigRepository.findAll();
        Assert.mustBeTrue(!CollectionUtils.isEmpty(all), "请检查平台配置");
        userEntity.setPassword(passwordEncoder.encode(all.get(0).getInitPassword()));
        userEntity.setPhone(Constant.DEFAULT_PHONE);
        userEntity.setEmail(Constant.DEFAULT_EMAIL);
        userEntity.setRole(sysRole);
        merchant.setUser(userEntity);
        save(merchant);
    }

    @Override
    public void update(MerchantReqParams reqParams, Long id) {
        MerchantEntity merchant = getById(id);
        BeanCopyUtils.copyBean(reqParams, merchant);
        save(merchant);
    }

    @Override
    public void delete(Long id) {
        deleteById(id);
    }

    @Override
    public MerchantDetailRespParams select(Long id) {
        return BeanCopyUtils.copyBean(getById(id), MerchantDetailRespParams.class);
    }

    @Override
    public Page<MerchantPageRespParams> selectPage(MerchantQuery query, PageReqParams reqParams) {
        Page<MerchantEntity> list = getPage(query, toPageable(reqParams, Constant.CREATE_TIME));
        return list.map(e -> pageCopy(e, MerchantPageRespParams.class));
    }

    @Override
    public Long getCountMerchant() {
        return count(new MerchantQuery());
    }

    @Autowired
    SysConfigRepository sysConfigRepository;

    @Autowired
    SysUserRepository sysUserRepository;

    @Autowired
    SysRoleRepository sysRoleRepository;

    @Autowired
    MerchantRepository merchantRepository;

    @Autowired
    SysUserService sysUserService;

    @Autowired
    CacheService cacheService;

    @Autowired
    PasswordEncoder passwordEncoder;

}
