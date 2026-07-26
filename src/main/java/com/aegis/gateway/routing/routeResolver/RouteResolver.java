package com.aegis.gateway.routing.routeResolver;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import reactor.core.publisher.Mono;

public interface RouteResolver {

    Mono<RouteDefinition> resolve(GatewayContext context);
}
