package com.qiyuesuo.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;


/**
 * 工具类使用第三方jar包 封装post请求 可以上传文件及数据
 */
public class ApaHttpsUtil {

    // 网络链接配置
    private static RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).setConnectionRequestTimeout(60000).build();

    /**
     * 发送Post
     *
     * @param url 请求地址
     * @param map 请求参数 文件类参数 key = file value = 文件地址
     * @param signature  MD5（token+secret+timestamp）
     * @param accesstoken  token
     * @param timestamp  时间戳
     * @return
     */
    public static String doPost(String url, Map<String, String> map, String signature, String accesstoken, String timestamp) {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setDefaultRequestConfig(requestConfig).build();
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        String httpStr = null;
        try {

            httpPost.setConfig(requestConfig);

            //设置请求头
            httpPost.setHeader("x-qys-open-signature", signature);
            httpPost.setHeader("x-qys-open-accesstoken", accesstoken);
            httpPost.setHeader("x-qys-open-timestamp", timestamp);


            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

            multipartEntityBuilder.setContentType(ContentType.create("multipart/form-data", "UTF-8"));

            //文件格式上传
            if (map.keySet().contains("file")) {
                multipartEntityBuilder.addBinaryBody("file", new File(map.get("file")));
                map.remove("file");
            }

            //数据上传
            for (Map.Entry<String, String> entry : map.entrySet()) {
                multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue(), ContentType.create("text/html", Charset.forName("utf-8")));
                //System.out.println("key= " + entry.getKey() + " and value= "+ entry.getValue());
            }

            HttpEntity httpEntity = multipartEntityBuilder.build();
            httpPost.setEntity(httpEntity);

            //发送请求
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            httpStr = EntityUtils.toString(entity);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpStr;
    }


    //创建SSL安全连接
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

                public void verify(String host, SSLSocket ssl) throws IOException {
                }

                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }
            });
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslsf;
    }


}
