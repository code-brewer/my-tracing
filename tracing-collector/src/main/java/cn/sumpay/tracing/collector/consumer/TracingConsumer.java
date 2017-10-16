package cn.sumpay.tracing.collector.consumer;

import cn.sumpay.tracing.collector.utils.HttpInvoker;
import cn.sumpay.tracing.collector.utils.StringUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
                String message = (String)jsonObject.get("message");
                if (StringUtil.isEmpty(message)){
                    return null;
                }
                Matcher matcher = TRACE_PATTERN.matcher(message);
                if (matcher.find()){
                    String traceInfo = matcher.group(1);
                    if (!traceInfo.startsWith("[")){
                        return "[" + traceInfo + "]";
                    }
                    return traceInfo;
                }
            }catch (Exception e){
                LOG.error("解析日志信息错误: {}, logInfo:",e.getMessage(),content);
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String content = "16:57:55.627 [http-nio-8000-exec-4] INFO trace [c0a808c0150581147544000310094] - {\"method\":\"getUser\"}";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message",content);
        TracingConsumer consumer = new TracingConsumer();
        String traceInfo = consumer.getTraceInfo(jsonObject.toJSONString());
        System.out.println(traceInfo);
    }
}
