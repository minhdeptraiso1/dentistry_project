package com.project.base_v1.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "payment.vn-pay")
public class VnPayConfig {
    private String tmnCode;
    private String secretKey;
    private String version;
    private String command;
    private String returnUrl;
    private String ipnUrl;
    private String vnpUrl;
    private String orderType;
}