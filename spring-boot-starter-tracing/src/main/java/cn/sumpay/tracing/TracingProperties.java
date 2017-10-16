package cn.sumpay.tracing;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

/**
 * @author heyc
 */
@ConfigurationProperties(prefix = "trace")
public class TracingProperties {

    private String serviceName;

    private boolean webEnable = true;

    private boolean webRequestEnable = false;

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

    public boolean isWebEnable() {
        return webEnable;
    }

    public void setWebEnable(boolean webEnable) {
        this.webEnable = webEnable;
    }

    public boolean isWebRequestEnable() {
        return webRequestEnable;
    }

    public void setWebRequestEnable(boolean webRequestEnable) {
        this.webRequestEnable = webRequestEnable;
    }
}
