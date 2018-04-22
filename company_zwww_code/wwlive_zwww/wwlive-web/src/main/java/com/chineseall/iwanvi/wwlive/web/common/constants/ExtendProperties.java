package com.chineseall.iwanvi.wwlive.web.common.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 加载扩展属性
 */
@Component("extendProperties")
public class ExtendProperties   {
    @Value("${push.redis.server}")
    private String pushRedisHost;
    @Value("${push.redis.port}")
    private int pushRedisPort;
    @Value("${push.redis.pwd}")
    private String pushReidsPwd;


    public String getPushRedisHost() {
        return pushRedisHost;
    }

    public void setPushRedisHost(String pushRedisHost) {
        this.pushRedisHost = pushRedisHost;
    }

    public int getPushRedisPort() {
        return pushRedisPort;
    }

    public void setPushRedisPort(int pushRedisPort) {
        this.pushRedisPort = pushRedisPort;
    }

    public String getPushReidsPwd() {
        return pushReidsPwd;
    }

    public void setPushReidsPwd(String pushReidsPwd) {
        this.pushReidsPwd = pushReidsPwd;
    }
}
