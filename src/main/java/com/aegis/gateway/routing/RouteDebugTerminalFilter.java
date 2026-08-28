package com.aegis.gateway.routing;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.pipeline.GatewayFilter;
import com.aegis.gateway.pipeline.GatewayFilterChain;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.aegis.gateway.context.GatewayContextAttributes.SELECTED_ROUTE;

@Component
public final class RouteDebugTerminalFilter implements GatewayFilter {

    @Override
    public int order() {
        return 10_000;
    }

    @Override
    public Mono<GatewayResponse> filter(GatewayContext context, GatewayFilterChain chain) {

        return context.getAttribute(SELECTED_ROUTE, RouteDefinition.class)
                .map(route -> Mono.just(
                        GatewayResponse.ok("Matched route %s -> %s".formatted(route.getId(), route.getTarget()))
                )).orElseGet(() -> Mono.just(
                        GatewayResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Route debug terminal reached without selected route")
                ));
    }
}