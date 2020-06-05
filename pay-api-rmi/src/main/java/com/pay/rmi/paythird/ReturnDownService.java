package com.pay.rmi.paythird;

import com.pay.rmi.api.resp.OrderApiRespParams;

public interface ReturnDownService {

    OrderApiRespParams returnDown(String result);
}
