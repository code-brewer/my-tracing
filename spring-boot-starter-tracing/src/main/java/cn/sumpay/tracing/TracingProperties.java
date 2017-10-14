package cn.sumpay.tracing;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

/**
 * @author heyc
 */
@ConfigurationProperties(prefix = "trace")
public class TracingProperties {

    private String serviceName;

    private boolean mvcRequestEnable;

    @PostConstruct
    public void init(){
        TracerConfig.APPLICATION = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isMvcRequestEnable() {
        return mvcRequestEnable;
    }

    public void setMvcRequestEnable(boolean mvcRequestEnable) {
        this.mvcRequestEnable = mvcRequestEnable;
    }
}
