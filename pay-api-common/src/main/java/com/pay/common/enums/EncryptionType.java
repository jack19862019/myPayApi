package com.pay.common.enums;

import javax.persistence.AttributeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum EncryptionType implements Serializable {
    MD5(99, "md5加密"),
    RSA(88, "rsa加密"),
    ;


    private Integer code;
    private String name;

    EncryptionType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<Integer, EncryptionType> map = new HashMap<>();

    private static Map<String, EncryptionType> mapString = new HashMap<>();


    public static EncryptionType getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (EncryptionType status : EncryptionType.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static EncryptionType getStatusByCode(int code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (EncryptionType status : EncryptionType.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(int code) {
        EncryptionType type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public int getCode(EncryptionType sex) {
        return sex.getCode();
    }


    public static class Convert implements AttributeConverter<EncryptionType, Integer> {
        @Override
        public Integer convertToDatabaseColumn(EncryptionType attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public EncryptionType convertToEntityAttribute(Integer dbData) {
            for (EncryptionType type : EncryptionType.values()) {
                if (dbData.equals(type.getCode())) {
                    return type;
                }
            }
            throw new RuntimeException("枚举值异常: " + dbData);
        }
    }
}
