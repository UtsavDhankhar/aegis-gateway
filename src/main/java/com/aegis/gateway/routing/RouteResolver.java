package com.aegis.gateway.routing;

import com.aegis.gateway.context.GatewayContext;
import reactor.core.publisher.Mono;

public interface RouteResolver {

    Mono<RouteDefinition> resolve(GatewayContext context);
}
