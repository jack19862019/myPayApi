package com.pay.manager.pc.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pay.common.enums.LogType;
import com.pay.data.entity.SysLogEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import com.tuyang.beanutils.annotation.CopyProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.util.Date;

@Getter
@Setter
@BeanCopySource(source = SysLogEntity.class)
public class SysLogUpDateRespParams {
    private Long id;

    private String username;

    private String method;

    private String browser;

    private String description;

    private LogType logType;

    @CopyProperty(ignored=true)
    private String exceptionDetail;

    public void setException(byte[] exceptionDetail) {
        String str = new String(exceptionDetail);
        this.exceptionDetail = str;
    }

    private String requestIp;

    private String params;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Long time;
}
