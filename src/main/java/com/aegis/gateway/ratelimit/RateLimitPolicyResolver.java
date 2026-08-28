package com.aegis.gateway.ratelimit;

import com.aegis.gateway.routing.RouteMetadataAccessor;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

import static com.aegis.gateway.ratelimit.RateLimitMetadataKeys.*;

@Component
public final class RateLimitPolicyResolver {

    private final RouteMetadataAccessor metadataAccessor;

    public RateLimitPolicyResolver(RouteMetadataAccessor metadataAccessor) {
        this.metadataAccessor = metadataAccessor;
    }

    public Optional<RateLimitPolicy> resolve(RouteDefinition route) {

        boolean enabled = metadataAccessor.getBoolean(route, ENABLED, false);

        if (!enabled) {
            return Optional.empty();
        }

        return Optional.of(
                new RateLimitPolicy(metadataAccessor.getLong(route, CAPACITY, 100),
                        metadataAccessor.getLong(route, REFILL_TOKENS, 100),
                        Duration.ofMillis(metadataAccessor.getLong(route, REFILL_PERIOD_MS, 60_000)),
                        metadataAccessor.getLong(route, TOKENS_PER_REQUEST, 1),
                        metadataAccessor.getString(route, KEY_RESOLVER).orElse("ip"),
                        metadataAccessor.getBoolean(route, FAIL_OPEN, true)));
    }
}
