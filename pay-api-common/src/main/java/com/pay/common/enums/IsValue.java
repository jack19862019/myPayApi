package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum IsValue implements Serializable {
    BC(-1, "报错"),
    ZC(1, "正常"),
    ;


    private Integer code;
    private String name;

    IsValue(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, IsValue> map = new HashMap<>();

    private static Map<String, IsValue> mapString = new HashMap<>();


    public static IsValue getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (IsValue status : IsValue.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static IsValue getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (IsValue status : IsValue.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        IsValue type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(IsValue sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<IsValue, Integer> {
        @Override
        public Integer convertToDatabaseColumn(IsValue attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public IsValue convertToEntityAttribute(Integer dbData) {
            for (IsValue type : IsValue.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
