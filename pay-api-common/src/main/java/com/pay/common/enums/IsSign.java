package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum IsSign implements Serializable {
    YES(-1, "参与签名"),
    NO(1, "不参与签名"),
    ;


    private Integer code;
    private String name;

    IsSign(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, IsSign> map = new HashMap<>();

    private static Map<String, IsSign> mapString = new HashMap<>();


    public static IsSign getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (IsSign status : IsSign.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static IsSign getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (IsSign status : IsSign.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        IsSign type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(IsSign sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<IsSign, Integer> {
        @Override
        public Integer convertToDatabaseColumn(IsSign attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public IsSign convertToEntityAttribute(Integer dbData) {
            for (IsSign type : IsSign.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
