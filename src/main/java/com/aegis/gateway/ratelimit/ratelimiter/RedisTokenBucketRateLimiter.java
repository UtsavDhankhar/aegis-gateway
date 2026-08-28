package com.aegis.gateway.ratelimit.ratelimiter;


import com.aegis.gateway.ratelimit.RateLimitDecision;
import com.aegis.gateway.ratelimit.RateLimitPolicy;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
public final class RedisTokenBucketRateLimiter implements RateLimiter {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RedisScript<String> tokenBucketScript;

    public RedisTokenBucketRateLimiter(ReactiveStringRedisTemplate redisTemplate, RedisScript<String> tokenBucketScript) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
    }

    @Override
    public Mono<RateLimitDecision> acquire(String key, RateLimitPolicy policy) {

        List<String> args = List.of(Long.toString(policy.capacity()),
                Long.toString(policy.refillTokens()),
                Long.toString(policy.refillPeriod().toMillis()),
                Long.toString(policy.tokensPerRequest()));

        return redisTemplate.execute(tokenBucketScript, List.of(key), args)
                .next()
                .switchIfEmpty(Mono.error(new IllegalStateException("Redis rate limiter returned no result")))
                .map(this::parseDecision);
    }

    private RateLimitDecision parseDecision(String value) {

        String[] parts = value.split("\\|");

        if (parts.length != 3) {
            throw new IllegalStateException("Invalid Redis rate limiter result: " + value);
        }

        boolean allowed = "1".equals(parts[0]);
        long remaining = Long.parseLong(parts[1]);
        long retryAfterMs = Long.parseLong(parts[2]);

        if (allowed) {
            return RateLimitDecision.allowed(remaining);
        }

        return RateLimitDecision.rejected(remaining, Duration.ofMillis(retryAfterMs));
    }
}