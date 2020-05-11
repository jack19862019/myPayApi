package com.pay.common.enums;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum DateWeekType implements Serializable {
    Monday(1, "星期一"),
    Tuesday(2, "星期二"),
    Wednesday(3, "星期三"),
    Thursday(4, "星期四"),

    Friday(5, "星期五"),
    Saturday(6, "星期六"),
    Sunday(7, "星期日"),
    ;


    private Integer code;
    private String name;

    DateWeekType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, DateWeekType> map = new HashMap<>();

    private static Map<String, DateWeekType> mapString = new HashMap<>();


    public static DateWeekType getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (DateWeekType status : DateWeekType.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static DateWeekType getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (DateWeekType status : DateWeekType.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        DateWeekType type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(DateWeekType sex) {
        return sex.getCode();
    }
}
