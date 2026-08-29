package com.aegis.gateway.health;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "aegis.gateway.health-check")
public record HealthCheckProperties(Boolean enabled,
                                    Duration interval,
                                    Duration timeout,
                                    Integer healthyThreshold,
                                    Integer unhealthyThreshold,
                                    String defaultPath) {

    public HealthCheckProperties {

        enabled = enabled == null || enabled;
        interval = interval == null ? Duration.ofSeconds(5) : interval;
        timeout = timeout == null ? Duration.ofSeconds(1) : timeout;
        healthyThreshold = healthyThreshold == null ? 2 : healthyThreshold;
        unhealthyThreshold = unhealthyThreshold == null ? 2 : unhealthyThreshold;
        defaultPath = defaultPath == null || defaultPath.isBlank() ? "/actuator/health/readiness" : defaultPath;
    }
}
