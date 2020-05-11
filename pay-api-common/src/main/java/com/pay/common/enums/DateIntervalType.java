package com.pay.common.enums;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum DateIntervalType implements Serializable {
    TODAY(0, "日"),
    WEEK(1, "周"),
    MONTH(2, "月"),
    YEAR(3, "年"),
    ;


    private Integer code;
    private String name;

    DateIntervalType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, DateIntervalType> map = new HashMap<>();

    private static Map<String, DateIntervalType> mapString = new HashMap<>();


    public static DateIntervalType getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (DateIntervalType status : DateIntervalType.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static DateIntervalType getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (DateIntervalType status : DateIntervalType.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        DateIntervalType type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(DateIntervalType sex) {
        return sex.getCode();
    }
}
