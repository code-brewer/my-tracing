package cn.sumpay.tracing.demo.service;

import cn.sumpay.tracing.trace.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author heyc
 */
@Service
public class HelloService {

    Logger LOG = LoggerFactory.getLogger(HelloService.class);

    @Trace(request = true,response = true)
    public String helloService(String say){
        LOG.info("系统日志：" + say);
        return say + " : -" + say;
    }

}
