package com.pay.rmi.api.resp;

import lombok.Data;

@Data
public class DecrypReqParams {

    private String sign;

    private String context;
}
