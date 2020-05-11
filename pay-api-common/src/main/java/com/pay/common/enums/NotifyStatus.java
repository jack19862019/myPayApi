package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum NotifyStatus implements Serializable {
    INIT(0, "初始化"),
    FAILURE(-1, "失败"),
    SUCCESS(1, "成功"),
    ;


    private Integer code;
    private String name;

    NotifyStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, NotifyStatus> map = new HashMap<>();

    private static Map<String, NotifyStatus> mapString = new HashMap<>();


    public static NotifyStatus getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (NotifyStatus status : NotifyStatus.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static NotifyStatus getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (NotifyStatus status : NotifyStatus.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        NotifyStatus type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(NotifyStatus sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<NotifyStatus, Integer> {
        @Override
        public Integer convertToDatabaseColumn(NotifyStatus attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public NotifyStatus convertToEntityAttribute(Integer dbData) {
            for (NotifyStatus type : NotifyStatus.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
