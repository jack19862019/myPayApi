package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum RoleType implements Serializable {
    MERCHANT(66, "商户"),
    MANAGER(55, "管理员");

    private Integer code;
    private String name;

    RoleType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, RoleType> map = new HashMap<>();

    private static Map<String, RoleType> mapString = new HashMap<>();


    public static RoleType getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (RoleType status : RoleType.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static RoleType getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (RoleType status : RoleType.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        RoleType type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(RoleType sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<RoleType, Integer> {
        @Override
        public Integer convertToDatabaseColumn(RoleType attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public RoleType convertToEntityAttribute(Integer dbData) {
            for (RoleType type : RoleType.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
