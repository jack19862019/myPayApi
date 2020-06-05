package com.pay.rmi.paythird;

import com.pay.rmi.api.resp.OrderApiRespParams;

import java.util.Map;

public interface RespService {

    OrderApiRespParams returnRespToDown(String result, OrderApiRespParams orderApiRespParams);

}
