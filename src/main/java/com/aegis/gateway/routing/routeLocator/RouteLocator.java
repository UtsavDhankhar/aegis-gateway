package com.aegis.gateway.routing.routeLocator;

import com.aegis.gateway.routing.RouteDefinition;
import reactor.core.publisher.Flux;

public interface RouteLocator {

    Flux<RouteDefinition> getRoutes();
}
