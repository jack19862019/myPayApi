package com.pay.rmi.common.utils;

import java.util.Map;
import java.util.Set;

public class BuildFormUtils {

    public static String buildForm(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        sb.append("<form>");
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            sb.append("<p>");
            sb.append(key);
            sb.append("：");
            sb.append(params.get(key));
            sb.append("</p>");
        }

        sb.append("</form>");
        return sb.toString();
    }


    public static String buildSubmitForm(String url, Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        sb.append("<form id=\"form\"");
        sb.append(" action=\"");
        sb.append(url);
        sb.append("\" method=\"post\">");
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(key);
            sb.append("\" value=\"");
            sb.append(params.get(key));
            sb.append("\">");
        }

        sb.append("</form>");
        sb.append("<script>document.getElementById('form').submit()</script>");
        return sb.toString();
    }


    public static String buildSubmitForm2(String img) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div style = \"text-align:center;width:1000px;height:1000px;margin-top:500px;\">");
        sb.append("<img style= \"width:50%;height:50%;\" src=\"");
        sb.append(img);
        sb.append("\"/>");
        sb.append("<p style = \"font-size:40px;font-weight:bold;\">长按二维码扫描 或 截图保存</p>");
        sb.append("<p style = \"font-size:40px;font-weight:bold;\">使用微信扫一扫-右上角点击 浏览器打开进行支付</p>");
        sb.append("</div>");
        return sb.toString();
    }
}
