package com.pay.rmi.api.resp;

import lombok.Data;

@Data
public class EncryReqParams {

    private MerchantReqParams params;

    private String merchantKey;
}
