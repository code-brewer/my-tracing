package cn.sumpay.tracing.demo.service;

import cn.sumpay.tracing.trace.Trace;
import org.springframework.stereotype.Service;

/**
 * @author heyc
 */
@Service
public class HelloService {

    @Trace
    public String helloService(String say){
        return say + " : -" + say;
    }

}
