package com.stockyard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "stockyard")
public record AppProperties(Jwt jwt, Cors cors, Uploads uploads) {
    public record Jwt(String secret, long expirationMinutes) {}
    public record Cors(List<String> allowedOrigins) {}
    public record Uploads(String dir, String publicBaseUrl) {}
}
