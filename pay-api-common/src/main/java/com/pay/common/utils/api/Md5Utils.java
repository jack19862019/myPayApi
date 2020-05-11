package com.pay.common.utils.api;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Md5Utils {

    private static Logger logger = LoggerFactory.getLogger(Md5Utils.class);

    public static String MD5(String content) {
        if (!StringUtils.isEmpty(content)) {
            try {
                return HexUtil.byte2hex(MessageDigest.getInstance("md5").digest(content.getBytes()));
            } catch (NoSuchAlgorithmException e) {
                logger.error("MD5加密错误！" + e.getMessage());
            }
        } else {
            logger.error("MD5加密内容为空！");
        }
        return null;
    }

    public static String SHA(String content) {
        if (!StringUtils.isEmpty(content)) {
            try {
                return HexUtil.byte2hex(MessageDigest.getInstance("SHA").digest(content.getBytes()));
            } catch (NoSuchAlgorithmException e) {
                logger.error("SHA加密错误！" + e.getMessage());
                throw new RuntimeException("SHA加密错误！" + e.getMessage());
            }
        } else {
            logger.error("SHA加密内容为空！");
        }
        return null;
    }

    public static boolean verify(String text, String sign, String key, String inputCharset) {
        text = text + key;
        String mysign = org.apache.commons.codec.digest.DigestUtils.md5Hex(getContentBytes(text, inputCharset));
        return mysign.equals(sign);
    }

    public static String sign(String text, String key, String inputCharset) {
        text = text + key;
        String mysign = DigestUtils.md5Hex(getContentBytes(text, inputCharset));
        return mysign;
    }


    public static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

    public static String sign(String paramStr, String key) {
        return getMD5(paramStr + key);
    }

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
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("sign error !");
        }
    }

    public static String sign(Map<String, String> params, String key) {
        return sign(buildParams(params), key);
    }

    public static String buildParams(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            String value = String.valueOf(params.get(key));
            if (org.apache.commons.lang3.StringUtils.isNotBlank(value) && !"null".equals(value)) {
                sb.append(key).append("=");
                sb.append(value);
                sb.append("&");
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
