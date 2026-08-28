package com.aegis.gateway.ratelimit.keyResolver;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import com.aegis.gateway.security.AuthenticatedPrincipal;
import reactor.core.publisher.Mono;

import static com.aegis.gateway.context.GatewayContextAttributes.AUTHENTICATED_PRINCIPAL;

public class PrincipalRateLimitKeyResolver implements  RateLimitKeyResolver {
    @Override
    public String getName() {
        return "principal";
    }

    @Override
    public Mono<String> resolve(GatewayContext gatewayContext, RouteDefinition routeDefinition) {
        return Mono.justOrEmpty(gatewayContext.getAttribute(AUTHENTICATED_PRINCIPAL, AuthenticatedPrincipal.class))
                .map(principal -> "principal=" + principal.subject());
    }
}
