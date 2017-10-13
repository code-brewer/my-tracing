package cn.sumpay.tracing.demo.controller;

import cn.sumpay.tracing.demo.service.HelloService;
import cn.sumpay.tracing.trace.Trace;
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

    @Autowired
    HelloService helloService;

    /**
     *
     * @param say
     * @return
     */
    @RequestMapping("hello")
    @ResponseBody
    @Trace
    public String helloController(String say){
       return helloService.helloService(say);
    }

}
