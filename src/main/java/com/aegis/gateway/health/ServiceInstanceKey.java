package com.aegis.gateway.health;

public record ServiceInstanceKey(
        String serviceId,
        String instanceId
) {
}