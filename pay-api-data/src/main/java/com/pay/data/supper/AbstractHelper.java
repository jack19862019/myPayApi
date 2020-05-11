package com.pay.data.supper;

import com.pay.common.enums.IsDelete;
import com.pay.common.enums.RoleType;
import com.pay.common.exception.Assert;
import com.pay.common.page.PageReqParams;
import com.pay.common.security.SecurityUtils;
import com.pay.common.security.UserInfo;
import com.pay.common.utils.ReflectUtils;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.SysUserEntity;
import com.pay.data.mapper.BaseRepository;
import com.pay.data.mapper.SysUserRepository;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

public abstract class AbstractHelper<R extends BaseRepository<T, A>, T, A> {

    @Autowired(required = false)
    protected R repository;

    @Autowired
    SysUserRepository sysUserRepository;

    private Class<T> getThisClass(int args) {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[args];
    }

    protected T getById(A id) {
        Optional<T> byId = repository.findById(id);
        Assert.mustBeTrue(byId.isPresent(), "你查询的" + getThisClass(1).getSimpleName() + "不存在:" + id);
        return byId.get();
    }

    protected void deleteById(A id) {
        T byId = getById(id);
        Object[] args = new Object[]{IsDelete.DELETE};
        ReflectUtils.invokeMethodByName(byId, "setIsDelete", args);
        save(byId);
    }

    protected Pageable toPageable(PageReqParams reqParams, String... sortStr) {
        return PageRequest.of(
                reqParams.getPageNumber() - 1, reqParams.getPageSize(),
                new Sort(Sort.Direction.DESC, sortStr)
        );
    }

    protected <S extends T> S save(S entity) {
        return repository.save(entity);
    }

    protected <Q> List<T> getList(Q criteria) {
        return repository.findAll((root, cq, cb) -> QueryHelp.getPredicate(root, criteria, cb));
    }

    protected <Q> List<T> getList(Q criteria, boolean flag) {
        return repository.findAll((root, cq, cb) -> QueryHelp.getPredicate(root, criteria, cb, flag));
    }

    protected <Q> List<T> getList(Q criteria, Sort sort) {
        return repository.findAll((root, cq, cb) -> QueryHelp.getPredicate(root, criteria, cb), sort);
    }

    protected <Q> Page<T> getPage(Q criteria, Pageable pageable) {
        return repository.findAll((root, cq, cb) -> QueryHelp.getPredicate(root, criteria, cb), pageable);
    }

    protected <Q> Long count(Q criteria){
        return repository.count((root, cq, cb) -> QueryHelp.getPredicate(root, criteria, cb));
    }

    protected Long count(){
        return repository.count();
    }

    protected <S> S pageCopy(T tClass, Class<S> sClass) {
        return BeanCopyUtils.copyBean(tClass, sClass);
    }

    protected MerchantEntity getMerchantByLogin(){
        UserInfo userDetails = SecurityUtils.getUserDetails();
        RoleType roleType = userDetails.getRoleType();
        String username = userDetails.getUsername();
        if (RoleType.MERCHANT.equals(roleType)) {
            SysUserEntity userEntity = sysUserRepository.findByUsername(username);
            MerchantEntity merchant = userEntity.getMerchant();
            Assert.isEmpty("该用户不是商户！", merchant);
            return merchant;
        }
        return null;
    }
}
