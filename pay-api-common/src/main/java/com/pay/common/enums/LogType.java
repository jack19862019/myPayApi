package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum LogType implements Serializable {
    INFO(1, "INFO"),
    ERROR(-1, "ERROR"),
    ;


    private Integer code;
    private String name;

    LogType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, LogType> map = new HashMap<>();

    private static Map<String, LogType> mapString = new HashMap<>();


    public static LogType getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (LogType status : LogType.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static LogType getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (LogType status : LogType.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        LogType type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(LogType sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<LogType, Integer> {
        @Override
        public Integer convertToDatabaseColumn(LogType attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public LogType convertToEntityAttribute(Integer dbData) {
            for (LogType type : LogType.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
