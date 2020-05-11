package com.pay.rmi.common.utils;

import com.alibaba.fastjson.JSON;
import com.pay.common.utils.api.Md5Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SignatureUtils {

    public static String getMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes("UTF-8"));
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("sign error !");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("sign error !");
        }
    }

    public static String buildParams(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            String value = String.valueOf(params.get(key));
            if (StringUtils.isNotBlank(value) && !"null".equals(value)) {
                sb.append(key).append("=");
                sb.append(value);
                sb.append("&");
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static String buildIgnoreNullParams(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            String value = String.valueOf(params.get(key));
            sb.append(key).append("=");
            sb.append(value);
            sb.append("&");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static String buildParams(Map<String, String> params, Boolean sort) {
        StringBuffer sb = new StringBuffer();
        List<String> keys = new ArrayList<>(params.keySet());
        if (sort) {
            Collections.sort(keys);
        }
        for (String key : keys) {
            String value = String.valueOf(params.get(key));
            if (StringUtils.isNotBlank(value) && !"null".equals(value)) {
                sb.append(key).append("=");
                sb.append(value);
                sb.append("&");
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static String buildParams3(Map<String, String> params, Boolean sort) {
        StringBuffer sb = new StringBuffer();
        List<String> keys = new ArrayList<>(params.keySet());
        if (sort) {
            Collections.sort(keys);
        }
        for (String key : keys) {
            String value = String.valueOf(params.get(key));
            if (StringUtils.isNotBlank(value) && !"null".equals(value)) {
                sb.append(key).append("=>");
                sb.append(value);
                sb.append("&");
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static String buildParams2(Map<String, String> params, Boolean sort) {
        StringBuffer sb = new StringBuffer();
        List<String> keys = new ArrayList<>(params.keySet());
        if (sort) {
            Collections.sort(keys);
        }
        for (String key : keys) {
            String value = String.valueOf(params.get(key));
            if (StringUtils.isNotBlank(value) && !"null".equals(value)) {
                sb.append(value);
            }
        }
        return sb.toString();
    }


    public static String md5MapSortSign(Map<String, String> paramsMap, String md5Key) {
        Map<String, String> result = new LinkedHashMap<>();
        paramsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
        String text = JSON.toJSONString(result);
        return Md5Utils.sign(text, md5Key, "UTF-8");
    }

    public static String sign(String paramStr, String key) {
        return getMD5(paramStr + key);
    }

    public static String sign(Map<String, String> params, String key) {
        return sign(buildParams(params), key);
    }

    public static String sign(Map<String, String> params, String key, boolean sort) {
        if (!sort) {
            String signParams = buildParams(params, sort);
            getMD5(signParams);
        }
        return sign(buildParams(params), key);

    }
}
