package com.pay.manager.pc.paylog;

import com.pay.common.constant.Constant;
import com.pay.common.page.PageReqParams;
import com.pay.data.entity.PayLogEntity;
import com.pay.data.mapper.PayLogRepository;
import com.pay.data.supper.AbstractHelper;
import com.tuyang.beanutils.BeanCopyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PayLogServiceImpl extends AbstractHelper<PayLogRepository, PayLogEntity, Long> implements PayLogService {

    @Autowired
    PayLogRepository payLogRepository;

    @Override
    public Page<PayLogRespParams> selectByFlagAndPayLogNo(PayLogQuery payLogQuery, PageReqParams reqParams)  {
        Page<PayLogEntity> list = getPage(payLogQuery, toPageable(reqParams, Constant.CREATE_TIME));
        return list.map(e -> pageCopy(e, PayLogRespParams.class));

    }

    @Override
    public void delete(Long id) {
        payLogRepository.deleteById(id);
    }

    @Override
    public PayLogRespParams select(Long id) {
        return BeanCopyUtils.copyBean(getById(id), PayLogRespParams.class);
    }

}
