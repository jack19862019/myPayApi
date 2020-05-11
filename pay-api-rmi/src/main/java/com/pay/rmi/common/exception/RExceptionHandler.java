package com.pay.rmi.common.exception;


import com.pay.common.utils.Result;
import com.pay.rmi.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestControllerAdvice
@Slf4j
public class RExceptionHandler {

    @ExceptionHandler(Exception.class)
    public R handleException(Exception e) {
        log.error(e.getMessage(), e);
        String stackTrace = getStackTrace(e);
        if (stackTrace.contains("Read timed out") || stackTrace.contains("connect timed out")) {
            String s = ((RException) e).getchannelNo();
            return R.error(s + ":支付请求超时，请重试。如多次请求失败，请联系第三方解决");
        }
        if (stackTrace.contains("failed to respond") || stackTrace.contains("NoHttpResponseException")) {
            String s = ((RException) e).getchannelNo();
            String message = e.getCause().getMessage();
            int noHttpResponseException = message.indexOf("NoHttpResponseException");
            String substring = message.substring(noHttpResponseException);
            return R.error(s + ":支付请求域名不可用:" + substring + "，请重试。如多次请求失败，请联系第三方解决");
        }
        if (stackTrace.contains("HttpServerErrorException") || stackTrace.contains("500 Internal Server Error")) {
            String s = ((RException) e).getchannelNo();
            return R.error(s + "第三方代码500报错，请联系第三方解决,第三方支付接口报错:" + e.getMessage());
        }
        return R.error(e.getMessage());
    }

    @ExceptionHandler(NumberFormatException.class)
    public Result NumberFormatException(NumberFormatException e) {
        log.error(e.getMessage(), e);
        return Result.error("数据类型Format错误:" + e.getMessage());
    }

    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            throwable.printStackTrace(pw);
            return sw.toString();
        } finally {
            pw.close();
        }
    }

}
