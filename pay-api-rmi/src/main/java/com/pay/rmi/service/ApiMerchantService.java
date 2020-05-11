package com.pay.rmi.service;

import com.pay.data.entity.MerchantEntity;

import java.util.List;

public interface ApiMerchantService {

    MerchantEntity selectByMerchantNo(String merchantNo);

    List<MerchantEntity> selectAllMerchant();

}
