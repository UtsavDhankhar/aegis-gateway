package com.aegis.gateway.health;

import java.time.Instant;

public record InstanceHealthState(
        InstanceHealthStatus status,
        int consecutiveSuccesses,
        int consecutiveFailures,
        Instant lastCheckedAt
) {

    public static InstanceHealthState unknown() {
        return new InstanceHealthState(
                InstanceHealthStatus.UNKNOWN,
                0,
                0,
                null
        );
    }
}
