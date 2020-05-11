package com.pay.rmi.common.utils;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SignUtils {

    public static final String MD5 = "MD5";


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

    public static String buildParamsIgnoreNull(Map<String, String> params) {
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

    public static String buildParamsSort(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        List<String> keys = new ArrayList<>(params.keySet());
        for (String key : keys) {
            String value = String.valueOf(params.get(key));
            sb.append(key).append("=");
            sb.append(value);
            sb.append("&");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static String buildParamsObject(Map<String, Object> params) {
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
}
