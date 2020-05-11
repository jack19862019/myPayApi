package com.pay.rmi.paythird.xunke.util;

public class StringUtils {

	public static String formatString(String text){ 
		if(text == null) {
			return ""; 
		}
		return text;
	}
	
	
	public static Boolean hasText(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return false;
		}
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}
	
	public static String[] strSplit(String str,String split){
		if(!StringUtils.hasText(str))return new String[]{};
		return str.split(split);
	}
}
