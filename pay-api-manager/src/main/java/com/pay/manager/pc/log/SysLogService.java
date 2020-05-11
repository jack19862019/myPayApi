package com.pay.manager.pc.log;


import com.pay.common.page.PageReqParams;
import com.pay.data.entity.SysLogEntity;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;

public interface SysLogService {
    void delete(Long id);

    Page<SysLogRespParams> getLogPage(LogQuery logQuery, PageReqParams reqParams);

    SysLogUpDateRespParams select(Long id);

    @Async
    void insert(String username, String browser, String ipAddr, ProceedingJoinPoint joinPoint, SysLogEntity log);
}
