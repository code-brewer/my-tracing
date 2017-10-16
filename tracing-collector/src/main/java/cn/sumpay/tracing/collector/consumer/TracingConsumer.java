package cn.sumpay.tracing.collector.consumer;

import cn.sumpay.tracing.collector.utils.HttpInvoker;
import cn.sumpay.tracing.collector.utils.StringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author heyc
 */
@Component
public class TracingConsumer {

    private static Logger LOG = LoggerFactory.getLogger(TracingConsumer.class);

    private static Pattern TRACE_PATTERN = Pattern.compile(".+\\s+trace\\s+\\[\\w+]\\s*-\\s*\\r?\\s*(\\[?\\{.+}]?)$");

    @Value("${zipkin.server.url:}")
    private String zipKinUrl;

    /**
     * consum
     * @param content
     */
    @KafkaListener(topics = "flume_log")
    public void consum(String content){
        LOG.debug("received message: {}",content);
        String trace = getTraceInfo(content);
        if (!StringUtil.isEmpty(trace)){
            try {
                LOG.info("解析trace完成: {}",content);
                String response = HttpInvoker.doPost(zipKinUrl + "/api/v2/spans", trace);
                LOG.info("收集trace成功: {}",response);
            }catch (Exception e){
                LOG.error("收集trace错误: {}",e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * getTraceInfo
     * @param content
     * @return
     */
    public String getTraceInfo(String content){
        if (!StringUtil.isEmpty(content)){
            try {
                JSONObject jsonObject = JSONObject.parseObject(content);
                String message = jsonObject.getString("message");
                String app = jsonObject.getString("app");
                if (StringUtil.isEmpty(message)){
                    return null;
                }
                Matcher matcher = TRACE_PATTERN.matcher(message);
                if (matcher.find()){
                    String traceInfo = matcher.group(1);
                    if (!traceInfo.startsWith("[")){
                        traceInfo = "[" + traceInfo + "]";
                    }
                    JSONArray jsonArray = JSONArray.parseArray(traceInfo);
                    Iterator<Object> iterator = jsonArray.iterator();
                    while (iterator.hasNext()){
                        JSONObject json = (JSONObject) iterator.next();
                        String name = json.getString("name");
                        JSONObject endpoint = json.getJSONObject("localEndpoint");
                        if (endpoint != null){
                            String serviceName = endpoint.getString("serviceName");
                            if (StringUtil.isEmpty(serviceName) || "unknown".equals(serviceName)){
                                endpoint.put("serviceName",app + ":" + name);
                            }
                        }
                    }
                    return jsonArray.toJSONString();
                }
            }catch (Exception e){
                LOG.error("解析日志信息错误: {}, logInfo:",e.getMessage(),content);
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String content = "16:57:55.627 [http-nio-8000-exec-4] INFO trace [c0a808c0150581147544000310094] -  \t {\"traceId\":\"9f2360dbc217e412\",\"id\":\"9f2360dbc217e412\",\"kind\":\"CLIENT\",\"name\":\"grant\",\"timestamp\":1508130188739273,\"duration\":302721,\"localEndpoint\":{\"serviceName\":\"unknown\",\"ipv4\":\"192.168.13.210\"},\"tags\":{\"class\":\"cn.sumpay.manage.role.controller.RoleController\",\"method\":\"grant\",\"request\":\"[14,[4,12,16,17,35,5,21,22,7,19,18,20,10,317,318,319,9,13,14,15,26,27,36,28,38,37,29,30,31,32,34,40,46,47,48,45,41,53,54,55,56,57,42,49,50,51,52,43]]\",\"response\":\"{\\\"msg\\\":\\\"授权成功\\\",\\\"success\\\":true}\",\"span.kind\":\"client\",\"type\":\"local\"}}";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message",content);
        jsonObject.put("app","tracingConsumer");
        TracingConsumer consumer = new TracingConsumer();
        String traceInfo = consumer.getTraceInfo(jsonObject.toJSONString());
        System.out.println(traceInfo);
    }
}
