package com.securevault.main.service.expiration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration properties for Redis entity discovery
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.redis.entity")
public class RedisEntityDiscoveryProperties {

    /**
     * Package patterns to scan for Redis entities
     * Default: com.securevault.main.entity
     * Example: com.securevault.main.entity,com.securevault.other.entity
     */
    private List<String> packagePatterns = List.of("com.securevault.main.entity");
} 