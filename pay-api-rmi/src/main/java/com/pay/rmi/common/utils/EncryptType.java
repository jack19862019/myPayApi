package com.pay.rmi.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName EncryptType
 * @Description 加密方式
 * @Date 2017年10月31日 上午11:20:35
 * @version 1.0.0
 */
public enum EncryptType {
	MD5,RSA;
	private static final Map<String,String> descMap = new HashMap<>(4);
	static{
		descMap.put(MD5.name(), "MD5");
		descMap.put(RSA.name(), "RSA");
	}
	public static Map<String, String> desc() {
		return descMap;
	}

	public static boolean isMD5(String encryptType) {
		if(MD5.name().equals(encryptType)) {
			return true;
		}else
			return false;
	}
}
