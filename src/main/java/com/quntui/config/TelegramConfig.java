package com.quntui.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "telegram")
public class TelegramConfig {
    private String botToken;
    private int apiId;
    private String apiHash;
    private Proxy proxy = new Proxy();

    @Data
    public static class Proxy {
        private boolean enabled;
        private String host;
        private int port;
    }
}