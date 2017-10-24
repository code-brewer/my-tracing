package cn.sumpay.tracing.demo.controller;

import cn.sumpay.tracing.demo.service.HelloService;
import com.alibaba.boot.dubbo.annotation.DubboConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author heyc
 */
@Controller
@RequestMapping("/")
public class HelloController {

    Logger LOG = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    HelloService helloService;

    /**
     *
     * @param say
     * @return
     */
    @RequestMapping("hello")
    @ResponseBody
    public String helloController(String say){

       LOG.info("系统日志：" + say);
       return helloService.helloService(say);
    }

}
