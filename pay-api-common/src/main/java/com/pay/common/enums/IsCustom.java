package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum IsCustom implements Serializable {
    DELETE(-1, "否"),
    NORMAL(1, "自定义"),
    ;


    private Integer code;
    private String name;

    IsCustom(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, IsCustom> map = new HashMap<>();

    private static Map<String, IsCustom> mapString = new HashMap<>();


    public static IsCustom getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (IsCustom status : IsCustom.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static IsCustom getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (IsCustom status : IsCustom.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        IsCustom type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(IsCustom sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<IsCustom, Integer> {
        @Override
        public Integer convertToDatabaseColumn(IsCustom attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public IsCustom convertToEntityAttribute(Integer dbData) {
            for (IsCustom type : IsCustom.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
