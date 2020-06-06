package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum IsOrder implements Serializable {
    BACK(-1, "回调"),
    ORDER(1, "下单"),
    ;


    private Integer code;
    private String name;

    IsOrder(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, IsOrder> map = new HashMap<>();

    private static Map<String, IsOrder> mapString = new HashMap<>();


    public static IsOrder getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (IsOrder status : IsOrder.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static IsOrder getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (IsOrder status : IsOrder.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        IsOrder type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(IsOrder sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<IsOrder, Integer> {
        @Override
        public Integer convertToDatabaseColumn(IsOrder attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public IsOrder convertToEntityAttribute(Integer dbData) {
            for (IsOrder type : IsOrder.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
