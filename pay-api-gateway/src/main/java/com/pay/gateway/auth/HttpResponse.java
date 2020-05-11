package com.pay.gateway.auth;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.ResultCode;
import com.pay.common.utils.Result;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HttpResponse {

    public void writer(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setStatus(200);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        String body = JSON.toJSONString(Result.authFailure(resultCode));
        printWriter.write(body);
        printWriter.flush();
    }
}
