package com.pay.manager.pc.upPayType;

import com.pay.common.page.PageReqParams;
import com.pay.manager.pc.type.params.PayTypePageRespParams;
import com.pay.manager.pc.type.params.PayTypeQuery;
import com.pay.manager.pc.upPayType.params.UpPayTypePageRespParams;
import com.pay.manager.pc.upPayType.params.UpPayTypeParams;
import com.pay.manager.pc.upPayType.params.UpPayTypeQuery;
import org.springframework.data.domain.Page;


public interface UpPayTypeService {

    void insert(UpPayTypeParams reqParams);

    void update(UpPayTypeParams reqParams, Long id);

    void delete(Long id);

    Page<UpPayTypePageRespParams> selectPage(UpPayTypeQuery query, PageReqParams reqParams);
}
