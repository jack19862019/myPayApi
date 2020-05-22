package com.pay.manager.pc.upPayType;

import com.pay.common.constant.Constant;
import com.pay.common.page.PageReqParams;
import com.pay.data.entity.PayTypeEntity;
import com.pay.data.entity.UpPayTypeEntity;
import com.pay.data.mapper.PayTypeRepository;
import com.pay.data.mapper.UpPayTypeRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.type.params.PayTypePageRespParams;
import com.pay.manager.pc.upPayType.params.UpPayTypePageRespParams;
import com.pay.manager.pc.upPayType.params.UpPayTypeParams;
import com.pay.manager.pc.upPayType.params.UpPayTypeQuery;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class UpPayTypeServiceImpl extends AbstractHelper<UpPayTypeRepository, UpPayTypeEntity, Long> implements UpPayTypeService {

    @Override
    public void insert(UpPayTypeParams upReqParams) {
        save(BeanCopyUtils.copyBean(upReqParams, UpPayTypeEntity.class));
    }

    @Override
    public void update(UpPayTypeParams upReqParams, Long id) {
        upReqParams.setId(id);
        UpPayTypeEntity upPayType = getById(id);
        BeanCopyUtils.copyBean(upReqParams, upPayType);
        save(upPayType);
    }

    @Override
    public void delete(Long id) {
        deleteById(id);
    }

    @Override
    public Page<UpPayTypePageRespParams> selectPage(UpPayTypeQuery query, PageReqParams reqParams) {
        Page<UpPayTypeEntity> list = getPage(query, toPageable(reqParams, Constant.CREATE_TIME));
        return list.map(e -> pageCopy(e, UpPayTypePageRespParams.class));
    }
}
