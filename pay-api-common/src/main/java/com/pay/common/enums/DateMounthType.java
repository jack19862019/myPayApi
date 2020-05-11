package com.pay.common.enums;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum DateMounthType implements Serializable {
    January(1, "一月份"),
    February(2, "二月份"),
    March(3, "三月份"),
    April(4, "四月份"),

    May(5, "五月份"),
    June(6, "六月份"),
    July(7, "七月份"),
    August(8, "八月份"),

    September(9, "九月份"),
    October(10, "十月份"),
    November(11, "十一月份"),
    December(12, "十二月份")
    ;


    private Integer code;
    private String name;

    DateMounthType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, DateMounthType> map = new HashMap<>();

    private static Map<String, DateMounthType> mapString = new HashMap<>();


    public static DateMounthType getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (DateMounthType status : DateMounthType.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static DateMounthType getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (DateMounthType status : DateMounthType.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        DateMounthType type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(DateMounthType sex) {
        return sex.getCode();
    }
}
