package com.qiyuesuo.client;

import com.qiyuesuo.constant.TokenAndSecret;
import com.qiyuesuo.util.MD5;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;


/**
 * post文件上传 JDK
 * @author SLX
 * @date 2018/11/22 9:59
 */
public class UploadFile {


    /**
     * 上传文件和数据
     * @param url 请求地址
     * @param file 所上传文件
     * @param params 上传其他参数 HashMap集合
     * @return
     */
    public static String uploadFile(String url, File file, Map<String, String> params) {

        String token = TokenAndSecret.token; //token
        String secret = TokenAndSecret.secret; //secret
        Long timestamp1 = System.currentTimeMillis(); //时间戳
        String timestamp = Long.toString(timestamp1);
        String tst = token + secret + timestamp;
        String signature = MD5.toMD5(tst);

        String message = "";
        String boundary = "WebKitFormBoundaryc9zQ3JDR9k3B5ARx";

        InputStream inputStream = null;
        FileInputStream fileInputStream = null;
        DataOutputStream dataOutputStream = null;
        HttpURLConnection connection = null;
        try {
            URL url1 = new URL(url);
            connection = (HttpURLConnection) url1.openConnection();
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Connection", "Keep-Alive");
            connection.addRequestProperty("Charset", "UTF-8");
            connection.addRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            //设置请求头
            connection.setRequestProperty("x-qys-open-signature", signature);
            connection.setRequestProperty("x-qys-open-accesstoken", token);
            connection.setRequestProperty("x-qys-open-timestamp", timestamp);

            // 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在
            // http正文内，因此需要设为true, 默认情况下是false;
            connection.setDoOutput(true);
            //设置是否从httpUrlConnection读入，默认情况下是true;
            connection.setDoInput(true);
            // Post 请求不能使用缓存  ?
            connection.setUseCaches(false);
            connection.setConnectTimeout(20000);
            dataOutputStream = new DataOutputStream(connection.getOutputStream());

            if (file != null) {
                //文件上传
                fileInputStream = new FileInputStream(file);
                dataOutputStream.writeBytes("--" + boundary + "\r\n");

                // 设定传送的内容类型是可序列化的java对象
                // (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + URLEncoder.encode(file.getName(), "UTF-8") + "\"\r\n");
                dataOutputStream.writeBytes("Content-Type: application/pdf"+"\r\n");
                dataOutputStream.writeBytes("\r\n");
                byte[] b = new byte[1024];
                while ((fileInputStream.read(b)) != -1) {
                    dataOutputStream.write(b);
                }
                dataOutputStream.writeBytes("\r\n");
            }

            //数据上传
            for (Map.Entry<String, String> entry:params.entrySet()){
                dataOutputStream.writeBytes("\r\n");
                dataOutputStream.writeBytes("--" + boundary + "\r\n");
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n");
                dataOutputStream.writeBytes("\r\n");
                dataOutputStream.writeBytes(entry.getValue() + "\r\n");
            }
            dataOutputStream.writeBytes("--" + boundary +"--"+ "\r\n");

            if (connection.getResponseCode() >= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + connection.getResponseCode());
            }


            if (connection.getResponseCode() == 200) {

                inputStream = connection.getInputStream();
                byte[] data = new byte[1024];
                StringBuffer sb1 = new StringBuffer();
                int length = 0;
                while ((length = inputStream.read(data)) != -1) {
                    String s = new String(data, Charset.forName("utf-8"));
                    sb1.append(s);
                }
                message = sb1.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            connection.disconnect();

        }
        return message;
    }

    /**
     * 参数加密
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }
}
