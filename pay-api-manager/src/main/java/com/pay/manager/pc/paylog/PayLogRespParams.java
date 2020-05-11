package com.pay.manager.pc.paylog;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pay.common.annotation.Query;
import com.pay.data.entity.SysLogEntity;
import com.tuyang.beanutils.annotation.BeanCopySource;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@BeanCopySource(source = SysLogEntity.class)
public class PayLogRespParams {
    private Long id;

    private String methodsName;
    @Query(type = Query.Type.IN, propName = "channelflag")
    private String channelFlag;

    private String channelName;

    private String logContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
