package com.aegis.gateway.ratelimit.ratelimiter;

import com.aegis.gateway.ratelimit.RateLimitDecision;
import com.aegis.gateway.ratelimit.RateLimitPolicy;
import reactor.core.publisher.Mono;

public interface RateLimiter {

    Mono<RateLimitDecision> acquire(String key, RateLimitPolicy policy);
}
