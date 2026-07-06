package com.aegis.gateway.routing.routeLocator;

import com.aegis.gateway.routing.RouteDefinition;
import com.aegis.gateway.routing.RouteLocator;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.aegis.gateway.routing.predicate.RoutePredicateFactory.method;
import static com.aegis.gateway.routing.predicate.RoutePredicateFactory.path;

@Component
public final class InMemoryRouteLocator implements RouteLocator {

    private final List<RouteDefinition> routes = List.of(
            RouteDefinition.builder()
                    .id("user-service-route")
                    .targetUri("http://localhost:9001")
                    .order(10)
                    .predicates(List.of(
                            path("/gateway/users/**"),
                            method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                    ))
                    .build(),

            RouteDefinition.builder()
                    .id("order-service-route")
                    .targetUri("http://localhost:9002")
                    .order(20)
                    .predicates(List.of(
                            path("/gateway/orders/**"),
                            method(HttpMethod.GET, HttpMethod.POST)
                    ))
                    .build()
    );

    @Override
    public Flux<RouteDefinition> getRoutes() {
        return Flux.fromIterable(routes);
    }
}