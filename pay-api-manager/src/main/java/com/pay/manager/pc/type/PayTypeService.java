package com.pay.manager.pc.type;

import com.pay.common.page.PageReqParams;
import com.pay.manager.pc.type.params.PayTypePageRespParams;
import com.pay.manager.pc.type.params.PayTypeQuery;
import com.pay.manager.pc.type.params.PayTypeParams;
import com.pay.manager.pc.type.params.PayTypeRespParams;
import org.springframework.data.domain.Page;


public interface PayTypeService {

    void insert(PayTypeParams reqParams);

    void update(PayTypeParams reqParams, Long id);

    void delete(Long id);

    PayTypeRespParams select(Long id);

    Page<PayTypePageRespParams> selectPage(PayTypeQuery query, PageReqParams reqParams);
}
