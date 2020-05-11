package com.pay.common.enums;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum MerChaType implements Serializable {
    MERCHANT(-1, "商户"),
    CHANNEL(1, "通道"),
    ;


    private Integer code;
    private String name;

    MerChaType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, MerChaType> map = new HashMap<>();

    private static Map<String, MerChaType> mapString = new HashMap<>();


    public static MerChaType getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (MerChaType status : MerChaType.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static MerChaType getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (MerChaType status : MerChaType.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        MerChaType type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(MerChaType sex) {
        return sex.getCode();
    }
}
