package cn.sumpay.tracing;

import com.alibaba.boot.dubbo.annotation.EnableDubboConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author heyc
 */
@SpringBootApplication(scanBasePackages = "cn.sumpay")
@EnableDubboConfiguration
public class TracingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TracingDemoApplication.class, args);
    }
}
