package com.pay.rmi.paythird.hanyinfu.util;



import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class JUtil {
	
	private static final ObjectMapper objmap = new ObjectMapper();



	/**
	 * map 转为json 字符串 默认编码utf-8
	 * 
	 * @param map
	 * @return
	 */
	public static String toJsonString(Map<String, Object> map) {
		return mapToJson(map, "UTF8");
	}

	public static String toJsonString2(Map<String, String> map) {
		return mapToJson2(map, "UTF8");
	}

	/**
	 * map 转为json 字符串
	 * 
	 * @param map
	 * @param charset
	 *            编码
	 * @return
	 */
	private static String mapToJson(Map<String, Object> map, String charset) {
		try {
			return new String(jsonFromObject(map, charset), charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String mapToJson2(Map<String, String> map, String charset) {
		try {
			return new String(jsonFromObject(map, charset), charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}



	public static byte[] jsonFromObject(Object paramObject, String paramString) {
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		JsonGenerator localJsonGenerator = null;
		try {
			localJsonGenerator = objmap.getJsonFactory().createJsonGenerator(
					localByteArrayOutputStream,
					JsonEncoding.valueOf(paramString));
			localJsonGenerator.writeObject(paramObject);
			localJsonGenerator.flush();
		} catch (RuntimeException localRuntimeException) {
			throw localRuntimeException;
		} catch (Exception localException) {
			
			return null;
		} finally {
			if (localJsonGenerator != null)
				try {
					localJsonGenerator.close();
				} catch (IOException localIOException2) {
				}
		}
		return localByteArrayOutputStream.toByteArray();
	}

	public static String jsonStrFromObject(Object paramObject,
                                           String paramString) {
		return new String(jsonFromObject(paramObject, paramString));

	}

}
