package com.pay.manager.pc.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pay.common.enums.LogType;
import com.pay.data.entity.SysLogEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@BeanCopySource(source = SysLogEntity.class)
public class SysLogRespParams {
    private Long id;

    private String username;

    private String method;

    private String browser;

    private String description;

    private LogType logType;

    private String requestIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Long time;
}
