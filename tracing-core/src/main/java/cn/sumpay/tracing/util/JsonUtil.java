package cn.sumpay.tracing.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author heyc
 * @date 2017/10/24 17:24
 */
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * toJsonString
     * @param object
     * @return
     */
    public static String toJsonString(Object object){
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * parseObject
     * @param jsonString
     * @param clazz
     * @return
     */
    public static <T> T  parseObject(String jsonString,Class<T> clazz){
        try {
            return mapper.readValue(jsonString,clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
