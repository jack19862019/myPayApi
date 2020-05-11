package com.pay.manager.pc.paylog;


import com.pay.common.page.PageReqParams;
import org.springframework.data.domain.Page;

public interface PayLogService {

    Page<PayLogRespParams> selectByFlagAndPayLogNo(PayLogQuery payLogQuery, PageReqParams reqParams);

    void delete(Long id);


    PayLogRespParams select(Long id);

}
