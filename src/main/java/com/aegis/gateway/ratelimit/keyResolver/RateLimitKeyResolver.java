package com.aegis.gateway.ratelimit.keyResolver;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import reactor.core.publisher.Mono;

public interface RateLimitKeyResolver {

    String getName();

    Mono<String> resolve(GatewayContext gatewayContext, RouteDefinition routeDefinition);
}
