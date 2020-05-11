package com.pay.rmi.api;

import javax.persistence.AttributeConverter;
import java.util.HashMap;
import java.util.Map;

public enum OrderEnum {
    ORDER(0, "下单检查"),
    OTHER(1, "其他检查"),
    ;


    private Integer code;
    private String name;

    OrderEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, OrderEnum> map = new HashMap<>();

    private static Map<String, OrderEnum> mapString = new HashMap<>();


    public static OrderEnum getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (OrderEnum status : OrderEnum.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static OrderEnum getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (OrderEnum status : OrderEnum.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        OrderEnum type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(OrderEnum sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<OrderEnum, Integer> {
        @Override
        public Integer convertToDatabaseColumn(OrderEnum attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public OrderEnum convertToEntityAttribute(Integer dbData) {
            for (OrderEnum type : OrderEnum.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("Unknown database value: " + dbData);
        }
    }

    public static boolean isValidName(OrderEnum sex) {
        for (OrderEnum sexEnum : OrderEnum.values()) {
            if (sex.equals(sexEnum)) {
                return true;
            }
        }
        return false;
    }
}
