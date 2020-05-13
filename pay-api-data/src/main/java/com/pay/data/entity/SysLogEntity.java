package com.pay.data.entity;

import com.pay.common.enums.LogType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;


/**
 * 系统日志
 */
@Getter
@Setter
@Entity
@Table(name = "sys_log",
        indexes = {
                @Index(columnList = "username"),
                @Index(columnList = "logType"),
                @Index(columnList = "requestIp"),
                @Index(name = "index_logType_username", columnList = "logType"),
                @Index(name = "index_logType_username", columnList = "username"),
                @Index(name = "index_logType_requestIp", columnList = "logType"),
                @Index(name = "index_logType_requestIp", columnList = "requestIp"),
        })
@Where(clause = "is_delete=1")
@NoArgsConstructor
public class SysLogEntity extends BaseEntity implements Serializable {

    // 操作用户
    private String username;

    // 描述
    private String description;

    // 方法名
    private String method;

    // 参数
    @Column(columnDefinition = "text")
    private String params;

    // 日志类型
    private LogType logType;

    // 请求ip
    private String requestIp;

    private String address;

    private String browser;

    // 请求耗时
    private Long time;

    // 异常详细
    @Column(name = "exception_detail", columnDefinition = "text")
    private byte[] exceptionDetail;

    public SysLogEntity(LogType logType, Long time) {
        this.logType = logType;
        this.time = time;
    }


}
