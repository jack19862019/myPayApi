package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum AuditStatus implements Serializable {
    AUDIT_WAIT(0, "待审核"),
    AUDIT_PASS(1, "已通过"),
    AUDIT_FAIL(-1, "未通过"),
    ;


    private Integer code;
    private String name;

    AuditStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, AuditStatus> map = new HashMap<>();

    private static Map<String, AuditStatus> mapString = new HashMap<>();


    public static AuditStatus getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (AuditStatus status : AuditStatus.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static AuditStatus getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (AuditStatus status : AuditStatus.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        AuditStatus type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(AuditStatus sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<AuditStatus, Integer> {
        @Override
        public Integer convertToDatabaseColumn(AuditStatus attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public AuditStatus convertToEntityAttribute(Integer dbData) {
            for (AuditStatus type : AuditStatus.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
