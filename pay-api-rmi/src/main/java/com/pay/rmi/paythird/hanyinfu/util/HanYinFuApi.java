package com.pay.rmi.paythird.hanyinfu.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class HanYinFuApi {

    // 接口地址
    private String apiURL;
    private HttpClient httpClient = null;
    private HttpPost httpPost = null;
    private HttpGet httpGet = null;
    private long startTime = 0L;
    private long endTime = 0L;
    private int status = 0;
    public final static String POST = "POST";
    public final static String GET = "GET";

    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_TEXT_JSON = "text/json";

    public HanYinFuApi() {

    }

    /**
     * 接口地址
     *
     * @param url
     */
    public HanYinFuApi(String url, String method) {

        if (url != null) {
            this.apiURL = url;
        }
        if (apiURL != null) {
            httpClient = new DefaultHttpClient();

            if (POST.equals(method)) {
                httpPost = new HttpPost(this.apiURL);
            } else if (GET.equals(method)) {
                httpGet = new HttpGet(url);
            }

        }
    }


    /*
     * post请求，
     */
    public String post(String parMap) {
        InputStream input = null;// 输入流
        InputStreamReader isr = null;
        BufferedReader buffer = null;
        StringBuffer sb = null;
        String line = null;
        try {
            /* post向服务器请求数据 */
            StringEntity entity = new StringEntity(parMap, "UTF-8");
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
            // StringEntity se = new StringEntity(parMap);
            // httpPost.setEntity(se);
            // httpPost.addHeader("Content-type",APPLICATION_JSON);
            // httpPost.setHeader("Accept", CONTENT_TYPE_TEXT_JSON);
            startTime = System.currentTimeMillis();
            HttpResponse response = httpClient.execute(httpPost);
            endTime = System.currentTimeMillis();
            int statusCode = response.getStatusLine().getStatusCode();

            /*System.out.println("HTTP响应码:" + statusCode);
            System.out.println("调用API 花费时间(单位：毫秒)：" + (endTime - startTime));*/

            // 若状态值为200，则ok
            if (statusCode == HttpStatus.SC_OK) {
                // 从服务器获得输入流
                input = response.getEntity().getContent();
                isr = new InputStreamReader(input);
                buffer = new BufferedReader(isr, 10 * 1024);

                sb = new StringBuffer();
                while ((line = buffer.readLine()) != null) {
                    sb.append(line);
                }
            }
        } catch (Exception e) {
            // 其他异常同样读取assets目录中的"local_stream.xml"文件
            //System.out.println("HttpClient post 数据异常");
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (buffer != null) {
                    buffer.close();
                    buffer = null;
                }
                if (isr != null) {
                    isr.close();
                    isr = null;
                }
                if (input != null) {
                    input.close();
                    input = null;
                }
            } catch (Exception e) {
            }
        }
        //System.out.println("PostData:" + sb.toString());
        return sb.toString();
    }

    public static String getSign(Map<String, String> map, String key) {
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() != "") {
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        int size = list.size();
        String[] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString() + "key=" + key;
        System.out.println(result);
        result = MD5.encryption(result).toUpperCase();
        return result;
    }


}
