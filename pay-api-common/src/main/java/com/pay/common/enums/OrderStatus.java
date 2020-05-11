package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

//init(0),succ(1), fail(2), ing(3), close(4);
public enum OrderStatus implements Serializable {
    init(0, "待支付"),
    succ(1, "支付成功"),
    fail(2, "支付失败"),
    ing(3, "处理中"),
    close(4, "关闭"),
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
