package com.pay.rmi.api.resp;

import lombok.Data;

@Data
public class MerchantResp {
    private Long id;

    private String name;

    private String num;

    private String publicKey;

}
