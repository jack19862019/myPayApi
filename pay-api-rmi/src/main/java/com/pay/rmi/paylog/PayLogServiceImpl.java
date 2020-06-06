package com.pay.rmi.paylog;

import com.pay.common.enums.IsOrder;
import com.pay.common.enums.IsValue;
import com.pay.data.entity.PayLogEntity;
import com.pay.data.mapper.ChannelRepository;
import com.pay.data.mapper.PayLogRepository;
import com.pay.data.supper.AbstractHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class PayLogServiceImpl extends AbstractHelper<PayLogRepository, PayLogEntity, Long> implements PayLogService {

    @Autowired
    PayLogRepository payLogRepository;


    @Override
    public void insertPayOrderLog(IsValue isValue, IsOrder isOrder, Integer sort, String... args) {
        PayLogEntity payLog = new PayLogEntity();
        payLog.setIsValue(isValue);
        payLog.setIsOrder(isOrder);
        payLog.setSort(sort);
        payLog.setMethod(args[0]);
        payLog.setOrderNo(args[1]);
        payLog.setChannelFlag(args[2]);
        payLog.setRGinseng(args[3]);
        payLog.setCGinseng(args[4]);

        payLog.setCreateUser(args[5]);
        payLog.setCreateTime(new Date());
        payLog.setUpdateUser(args[5]);
        payLog.setUpdateTime(new Date());
        payLogRepository.save(payLog);
    }
}
