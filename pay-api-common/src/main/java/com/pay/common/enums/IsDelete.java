package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum IsDelete implements Serializable {
    DELETE(-1, "删除"),
    NORMAL(1, "正常"),
    ;


    private Integer code;
    private String name;

    IsDelete(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, IsDelete> map = new HashMap<>();

    private static Map<String, IsDelete> mapString = new HashMap<>();


    public static IsDelete getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (IsDelete status : IsDelete.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static IsDelete getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (IsDelete status : IsDelete.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        IsDelete type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(IsDelete sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<IsDelete, Integer> {
        @Override
        public Integer convertToDatabaseColumn(IsDelete attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public IsDelete convertToEntityAttribute(Integer dbData) {
            for (IsDelete type : IsDelete.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
