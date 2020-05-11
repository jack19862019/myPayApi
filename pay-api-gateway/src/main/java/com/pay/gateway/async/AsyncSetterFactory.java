package com.pay.gateway.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class AsyncSetterFactory {

    private Executor executor;

    private int timeout;


    public AsyncSetterFactory (Executor executor, int timeout){
        this.executor = executor;
        this.timeout = timeout;
        Logger log = LoggerFactory.getLogger(AsyncSetterFactory.class);
        log.debug("AsyncSetter的timeout值为{}", this.timeout);
    }

    public AsyncSetter getAsyncSetter(){
        return new AsyncSetter(executor, timeout);
    }
}
