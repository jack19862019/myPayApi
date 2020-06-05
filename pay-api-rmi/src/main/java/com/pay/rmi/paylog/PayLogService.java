package com.pay.rmi.paylog;


import com.pay.common.enums.IsValue;
import org.springframework.scheduling.annotation.Async;

public interface PayLogService {

    @Async
    void insertPayOrderLog(IsValue isValue, Integer sort, String... args);
}
