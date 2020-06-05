package com.pay.rmi.paythird;

import java.util.Map;

public interface CallBackService {

    boolean verifySignParams(Map<String, String> params);

    String updateOrder(Map<String, String> params);

}
