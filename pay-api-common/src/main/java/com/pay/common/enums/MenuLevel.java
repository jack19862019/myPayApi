package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.util.HashMap;
import java.util.Map;

public enum MenuLevel {
    TITLE(1, "目录"),
    MENU(2, "菜单"),
    BUTTON(3, "按钮"),
    ;


    private Integer code;
    private String name;

    MenuLevel(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, MenuLevel> map = new HashMap<>();

    private static Map<String, MenuLevel> mapString = new HashMap<>();


    public static MenuLevel getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (MenuLevel status : MenuLevel.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static MenuLevel getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (MenuLevel status : MenuLevel.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        MenuLevel type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(MenuLevel sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<MenuLevel, Integer> {
        @Override
        public Integer convertToDatabaseColumn(MenuLevel attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public MenuLevel convertToEntityAttribute(Integer dbData) {
            for (MenuLevel type : MenuLevel.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
