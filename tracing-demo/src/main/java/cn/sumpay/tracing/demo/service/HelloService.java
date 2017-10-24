package cn.sumpay.tracing.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author heyc
 */
@Service
public class HelloService {

    Logger LOG = LoggerFactory.getLogger(HelloService.class);

    public String helloService(String say){
        LOG.info("系统日志：" + say);
        return say + " : -" + say;
    }

}
