package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum AmountType implements Serializable {
    FIXED(0, "固码"),
    RANGE(1, "范围"),
    MULTIPLE(2, "倍数"),
    ;


    private Integer code;
    private String name;

    AmountType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, AmountType> map = new HashMap<>();

    private static Map<String, AmountType> mapString = new HashMap<>();


    public static AmountType getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (AmountType status : AmountType.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static AmountType getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (AmountType status : AmountType.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        AmountType type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(AmountType sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<AmountType, Integer> {
        @Override
        public Integer convertToDatabaseColumn(AmountType attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public AmountType convertToEntityAttribute(Integer dbData) {
            for (AmountType type : AmountType.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
