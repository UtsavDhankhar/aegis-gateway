package com.aegis.gateway.routing;

import reactor.core.publisher.Flux;

public interface RouteLocator {

    Flux<RouteDefinition> getRoutes();
}
