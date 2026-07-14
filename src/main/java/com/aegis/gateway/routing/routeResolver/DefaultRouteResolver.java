package com.aegis.gateway.routing.routeResolver;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.routing.RouteDefinition;
import com.aegis.gateway.routing.routeLocator.RouteLocator;
import com.aegis.gateway.routing.RouteResolver;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;


@Service
public class DefaultRouteResolver implements RouteResolver {

    private final RouteLocator routeLocator;

    public DefaultRouteResolver(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Override
    public Mono<RouteDefinition> resolve(GatewayContext context) {
        return routeLocator.getRoutes()
                .filter(route -> route.matches(context))
                .sort(Comparator.comparingInt(RouteDefinition::getOrder))
                .next();
    }
}
