package com.aegis.gateway.ratelimit.keyResolver;


import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class IpRateLimitKeyResolver implements RateLimitKeyResolver {

    @Override
    public String getName() {
        return "ip";
    }

    @Override
    public Mono<String> resolve(GatewayContext gatewayContext, RouteDefinition routeDefinition) {

        return Mono.justOrEmpty(gatewayContext.request().remoteAddress())
                .map(address -> "ip:" + address.getHostString());
    }
}
