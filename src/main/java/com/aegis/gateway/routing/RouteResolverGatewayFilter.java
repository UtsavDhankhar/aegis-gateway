package com.aegis.gateway.routing;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.pipeline.GatewayFilter;
import com.aegis.gateway.pipeline.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.aegis.gateway.context.GatewayContextAttributes.SELECTED_ROUTE;

@Component
public final class RouteResolverGatewayFilter implements GatewayFilter {

    private final RouteResolver routeResolver;

    public RouteResolverGatewayFilter(RouteResolver routeResolver) {
        this.routeResolver = routeResolver;
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public Mono<GatewayResponse> filter(GatewayContext context,GatewayFilterChain chain) {
        return routeResolver.resolve(context)
                .flatMap(route -> {
                    context.putAttribute(SELECTED_ROUTE, route);
                    return chain.next(context);
                })
                .switchIfEmpty(Mono.just(
                        GatewayResponse.error(HttpStatus.NOT_FOUND, "No route matched")
                ));
    }
}