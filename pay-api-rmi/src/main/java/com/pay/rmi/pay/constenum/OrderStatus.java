package com.pay.rmi.pay.constenum;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum OrderStatus implements Serializable {
    REQUEST_SUCCESSFUL(0, "请求成功"),
    CALLBACK_SUCCESSFUL(1, "回调成功"),
    NOTICE_SUCCESSFUL(2, "通知成功"),
    ;


    private Integer code;
    private String name;

    OrderStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, OrderStatus> map = new HashMap<>();

    private static Map<String, OrderStatus> mapString = new HashMap<>();


    public static OrderStatus getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (OrderStatus status : OrderStatus.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static OrderStatus getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (OrderStatus status : OrderStatus.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        OrderStatus type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(OrderStatus sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<OrderStatus, Integer> {
        @Override
        public Integer convertToDatabaseColumn(OrderStatus attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public OrderStatus convertToEntityAttribute(Integer dbData) {
            for (OrderStatus type : OrderStatus.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
