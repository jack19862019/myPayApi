package com.pay.manager.pc.type;

import com.pay.common.constant.Constant;
import com.pay.common.page.PageReqParams;
import com.pay.data.entity.PayTypeEntity;
import com.pay.data.mapper.PayTypeRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.type.params.PayTypePageRespParams;
import com.pay.manager.pc.type.params.PayTypeQuery;
import com.pay.manager.pc.type.params.PayTypeParams;
import com.pay.manager.pc.type.params.PayTypeRespParams;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class PayTypeServiceImpl extends AbstractHelper<PayTypeRepository, PayTypeEntity, Long> implements PayTypeService {

    @Override
    public void insert(PayTypeParams reqParams) {
        save(BeanCopyUtils.copyBean(reqParams, PayTypeEntity.class));
    }

    @Override
    public void update(PayTypeParams reqParams, Long id) {
        reqParams.setId(id);
        PayTypeEntity payType = getById(id);
        BeanCopyUtils.copyBean(reqParams, payType);
        save(payType);
    }

    @Override
    public void delete(Long id) {
        deleteById(id);
    }

    @Override
    public PayTypeRespParams select(Long id) {
        return BeanCopyUtils.copyBean(getById(id), PayTypeRespParams.class);
    }

    @Override
    public Page<PayTypePageRespParams> selectPage(PayTypeQuery query, PageReqParams reqParams) {
        Page<PayTypeEntity> list = getPage(query, toPageable(reqParams, Constant.CREATE_TIME));
        return list.map(e -> pageCopy(e, PayTypePageRespParams.class));
    }
}
