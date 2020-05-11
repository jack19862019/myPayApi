package com.pay.rmi.paylog;


import org.springframework.scheduling.annotation.Async;

public interface PayLogService {

    @Async
    void insert(String name, String msg,  Object... arguments);
}
