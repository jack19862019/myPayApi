package com.pay.rmi.pay.constenum;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum OrderType implements Serializable {
    WITHDRAWAL(-1, "提现订单"),
    PAY(1, "支付订单"),
    ;


    private Integer code;
    private String name;

    OrderType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, OrderType> map = new HashMap<>();

    private static Map<String, OrderType> mapString = new HashMap<>();


    public static OrderType getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (OrderType status : OrderType.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static OrderType getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (OrderType status : OrderType.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        OrderType type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(OrderType sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<OrderType, Integer> {
        @Override
        public Integer convertToDatabaseColumn(OrderType attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public OrderType convertToEntityAttribute(Integer dbData) {
            for (OrderType type : OrderType.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
