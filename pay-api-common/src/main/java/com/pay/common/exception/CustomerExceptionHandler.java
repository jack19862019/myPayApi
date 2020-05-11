package com.pay.common.exception;

import com.pay.common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@Slf4j
@RestControllerAdvice
public class CustomerExceptionHandler {
    /**
     * 自定义异常
     */
    @ExceptionHandler(Exception.class)
    public Result CustomerException(Exception e) {
        if (e instanceof CustomerException) {
            CustomerException ex = (CustomerException) e;
            return Result.error(ex.getCode(), ex.getMsg());
        }
        e.printStackTrace();
        return Result.error(-99, "未知异常" + e);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result DuplicateKeyException(DuplicateKeyException e) {
        return Result.error("数据库中已存在该记录");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result IllegalArgumentException(IllegalArgumentException e) {
        return Result.error("参数异常！" + e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result DataIntegrityViolationException(DataIntegrityViolationException e) {
        Throwable mostSpecificCause = e.getMostSpecificCause();
        return Result.error("编辑数据异常:" + mostSpecificCause.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return Result.error("参数类型不匹配！" + e.getLocalizedMessage());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, NumberFormatException.class})
    public Result MethodArgumentTypeMismatchException(Exception e) {
        return Result.error("参数类型不匹配！" + e.getMessage());
    }
}
