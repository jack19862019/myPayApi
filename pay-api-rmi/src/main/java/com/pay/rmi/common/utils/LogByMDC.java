package com.pay.rmi.common.utils;

import com.pay.rmi.paylog.PayLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class LogByMDC {

    @Autowired
    private PayLogService payLogService;

    // 声明对象
    public static LogByMDC logByMDC;

    @PostConstruct // 初始化
    public void init(){
        logByMDC = this;
        logByMDC.payLogService = this.payLogService;
    }

    private static final String MDC_KEY = "third";

    private static final Logger logger = LoggerFactory.getLogger("thirdLog");

    public static void trace(String name, String msg, Object... arguments) {
        MDC.put(MDC_KEY, name);
        logger.trace(msg, arguments);
    }

    public static void trace(String name, String msg, Throwable t) {
        MDC.put(MDC_KEY, name);
        logger.trace(msg, t);
    }

    public static void debug(String name, String msg, Object... arguments) {
        MDC.put(MDC_KEY, name);
        logger.debug(msg, arguments);
    }

    public static void debug(String name, String msg, Throwable t) {
        MDC.put(MDC_KEY, name);
        logger.debug(msg, t);
    }

    public static void info(String name, String msg, Object... arguments) {
        logByMDC.payLogService.insert(name,msg, arguments);
        MDC.put(MDC_KEY, name);
        logger.info(msg, arguments);
    }

    public static void info(String name, String msg, Throwable t) {
        MDC.put(MDC_KEY, name);
        logger.info(msg, t);
    }

    public static void warn(String name, String msg, Object... arguments) {
        MDC.put(MDC_KEY, name);
        logger.warn(msg, arguments);
    }

    public static void warn(String name, String msg, Throwable t) {
        MDC.put(MDC_KEY, name);
        logger.warn(msg, t);
    }

    public static void error(String name, String msg, Object... arguments) {
        logByMDC.payLogService.insert(name,msg, arguments);
        MDC.put(MDC_KEY, name);
        logger.error(msg, arguments);
    }

    public static void error(String name, String msg, Throwable t) {
        MDC.put(MDC_KEY, name);
        logger.error(msg, t);
    }
}
