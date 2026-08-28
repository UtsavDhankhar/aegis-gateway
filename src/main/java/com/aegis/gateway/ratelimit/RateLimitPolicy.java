package com.aegis.gateway.ratelimit;

import java.time.Duration;

public record RateLimitPolicy(long capacity,
                              long refillTokens,
                              Duration refillPeriod,
                              long tokensPerRequest,
                              String keyResolver,
                              boolean failOpen) {

    public RateLimitPolicy {

        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }

        if (refillTokens <= 0) {
            throw new IllegalArgumentException("refillTokens must be > 0");
        }

        if (refillPeriod == null || refillPeriod.isZero() || refillPeriod.isNegative()) {
            throw new IllegalArgumentException("refillPeriod must be positive");
        }

        if (tokensPerRequest <= 0 || tokensPerRequest > capacity) {
            throw new IllegalArgumentException("tokensPerRequest must be between 1 and capacity");
        }
    }
}
