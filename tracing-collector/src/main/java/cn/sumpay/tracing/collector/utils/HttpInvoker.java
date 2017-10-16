package cn.sumpay.tracing.collector.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by heyc on 2017/9/19.
 */
public class HttpInvoker {

    public static String doGet(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpGet httpget = new HttpGet(url);
            response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        }finally {
            if (response != null)
                response.close();
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String doPost(String url, String jsonString) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpPost method = new HttpPost(url);
            if (null != jsonString) {
                StringEntity entity = new StringEntity(jsonString, "UTF-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                method.setEntity(entity);
            }
            response = httpClient.execute(method);
            return EntityUtils.toString(response.getEntity());
        }finally {
            if (response != null){
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
            if (httpClient != null){
                try {
                    httpClient.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
