package com.pay.rmi.common.utils;

import com.pay.rmi.common.exception.RException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class MapToXml {

    //Map转换成XML String
    public static String toString(Map<String, String> map) {
        if (map == null || map.size() == 0) {
            return "<xml></xml>";
        }
        ByteArrayOutputStream outputStream = null;
        XMLWriter writer = null;
        String xml = null;
        try {
            outputStream = new ByteArrayOutputStream();
            writer = new XMLWriter(outputStream);

            Element root = DocumentHelper.createElement("xml");
            map.forEach((key, val) -> {
                Element childElm;
                if (StringUtils.isNotEmpty(key) && val != null) {
                    childElm = DocumentHelper.createElement(key);
                    childElm.setText(val.toString());
                    root.add(childElm);
                }
            });
            Document document = DocumentHelper.createDocument(root);
            writer.write(document);
            writer.flush();
        } catch (Exception ex) {
            throw new RException("map转xml字符异常");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (outputStream != null) {
                    xml = outputStream.toString();
                    outputStream.close();
                }
            } catch (Exception e) {
                throw new RException("map转xml字符流关闭异常");
            }
        }
        return xml;
    }

    //XML String转换成Map
    public static Map<String, String> toMap(String xml) {
        try {
            if (StringUtils.isEmpty(xml)) {
                return new HashMap<>();
            }
            Document document = DocumentHelper.parseText(xml);
            Element root = document.getRootElement();

            Map<String, String> resultMap = new HashMap<>();
            for (Iterator it = root.elementIterator(); it.hasNext(); ) {
                Element element = (Element) it.next();
                String name = element.getName();
                String value = element.getTextTrim();
                if (StringUtils.isNoneEmpty(name, value)) {
                    resultMap.put(name, value);
                }
            }
            return resultMap;
        } catch (Exception e) {
            throw new RException("xml转map异常");
        }
    }


    //获取回调通知报文
    public static String getRequestBody(HttpServletRequest request) {
        StringBuilder body = new StringBuilder();
        try {
            BufferedReader br = request.getReader();
            while (true) {
                String info = br.readLine();
                if (info == null) {
                    break;
                }
                body.append(info);
            }
            br.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body.toString();
    }
}
