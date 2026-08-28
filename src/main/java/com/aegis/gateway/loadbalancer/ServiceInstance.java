package com.aegis.gateway.loadbalancer;


import java.net.URI;
import java.util.Map;

public record ServiceInstance(String instanceId,
                              String serviceId,
                              URI baseUri,
                              int weight,
                              boolean enabled,
                              Map<String, Object> metadata) {

    public ServiceInstance {

        if (instanceId == null || instanceId.isBlank()) {
            throw new IllegalArgumentException("instanceId must not be blank");
        }

        if (serviceId == null || serviceId.isBlank()) {
            throw new IllegalArgumentException("serviceId must not be blank");
        }

        if (baseUri == null) {
            throw new IllegalArgumentException("baseUri must not be null");
        }

        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be > 0");
        }

        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
