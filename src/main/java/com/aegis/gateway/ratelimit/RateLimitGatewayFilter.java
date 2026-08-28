package com.aegis.gateway.ratelimit;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.pipeline.GatewayFilter;
import com.aegis.gateway.pipeline.GatewayFilterChain;
import com.aegis.gateway.ratelimit.keyResolver.RateLimitKeyResolver;
import com.aegis.gateway.ratelimit.keyResolver.RateLimitKeyResolverRegistry;
import com.aegis.gateway.ratelimit.ratelimiter.RateLimiter;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static com.aegis.gateway.context.GatewayContextAttributes.RATE_LIMIT_DECISION;
import static com.aegis.gateway.context.GatewayContextAttributes.SELECTED_ROUTE;

@Component
public final class RateLimitGatewayFilter implements GatewayFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitGatewayFilter.class);

    private final RateLimitPolicyResolver policyResolver;
    private final RateLimitKeyResolverRegistry keyResolverRegistry;
    private final RateLimiter rateLimiter;

    public RateLimitGatewayFilter(RateLimitPolicyResolver policyResolver,
                                  RateLimitKeyResolverRegistry keyResolverRegistry,
                                  RateLimiter rateLimiter) {

        this.policyResolver = policyResolver;
        this.keyResolverRegistry = keyResolverRegistry;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public int order() {
        return 400;
    }

    @Override
    public Mono<GatewayResponse> filter(GatewayContext context, GatewayFilterChain chain) {

        Optional<RouteDefinition> routeOptional = context.getAttribute(SELECTED_ROUTE, RouteDefinition.class);

        if (routeOptional.isEmpty()) {
            return Mono.just(GatewayResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Rate limiter reached without selected route"));
        }

        RouteDefinition route = routeOptional.get();
        Optional<RateLimitPolicy> policyOptional = policyResolver.resolve(route);

        if (policyOptional.isEmpty()) {
            return chain.next(context);
        }

        RateLimitPolicy policy = policyOptional.get();
        RateLimitKeyResolver keyResolver = keyResolverRegistry.get(policy.keyResolver());

        return keyResolver.resolve(context, route)
                .switchIfEmpty(Mono.error(new IllegalStateException("Unable to resolve rate-limit identity")))
                .flatMap(identity -> {
                    String redisKey = buildRedisKey(route, identity);
                    return rateLimiter.acquire(redisKey, policy);
                })
                .flatMap(decision -> {
                    context.putAttribute(RATE_LIMIT_DECISION, decision);
                    if (!decision.allowed()) {
                        return rateLimitedResponse(policy, decision);
                    }

                    return chain.next(context);
                })
                .onErrorResume(error -> handleRateLimiterFailure(context, policy, chain, error));
    }

    private String buildRedisKey(RouteDefinition route, String identity) {
        return "aegis:ratelimit:" + route.getId() + ":" + identity;
    }

    private Mono<GatewayResponse> rateLimitedResponse(RateLimitPolicy policy, RateLimitDecision decision) {

        HttpHeaders headers = new HttpHeaders();

        long retryAfterSeconds = Math.max(1, (long) Math.ceil(decision.retryAfter().toMillis() / 1000.0));

        headers.set(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds));
        headers.set("X-RateLimit-Limit", Long.toString(policy.capacity()));
        headers.set("X-RateLimit-Remaining", Long.toString(decision.remainingTokens()));

        return Mono.just(new GatewayResponse.SimpleGatewayResponse(HttpStatus.TOO_MANY_REQUESTS, headers,
                Map.of("status", 429, "code", "RATE_LIMIT_EXCEEDED", "message", "Too many requests")));
    }

    private Mono<GatewayResponse> handleRateLimiterFailure(GatewayContext context, RateLimitPolicy policy, GatewayFilterChain chain, Throwable error) {

        LOGGER.atError()
                .addKeyValue("requestId", context.request().requestId())
                .addKeyValue("failOpen", policy.failOpen())
                .setCause(error).log("gateway.ratelimit.failed");

        if (policy.failOpen()) {
            return chain.next(context);
        }

        return Mono.just(GatewayResponse.error(HttpStatus.SERVICE_UNAVAILABLE, "Rate limiting service unavailable"));
    }
}
