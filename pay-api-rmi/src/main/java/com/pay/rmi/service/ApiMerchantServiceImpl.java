package com.pay.rmi.service;


import com.pay.data.entity.MerchantEntity;
import com.pay.data.mapper.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiMerchantServiceImpl implements ApiMerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    @Override
    public MerchantEntity selectByMerchantNo(String merchantNo) {
        return merchantRepository.findByMerchantNo(merchantNo);
    }

    @Override
    public List<MerchantEntity> selectAllMerchant() {
        return merchantRepository.findAll();
    }

}
