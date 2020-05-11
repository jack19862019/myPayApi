package com.pay.manager.pc.merchant;

import com.pay.common.page.PageReqParams;
import com.pay.manager.pc.merchant.params.*;
import org.springframework.data.domain.Page;

public interface MerchantService {

    void insert(MerchantReqParams reqParams);

    void update(MerchantReqParams reqParams, Long id);

    void delete(Long id);

    MerchantDetailRespParams select(Long id);

    Page<MerchantPageRespParams> selectPage(MerchantQuery query, PageReqParams reqParams);

    Long getCountMerchant();
}
