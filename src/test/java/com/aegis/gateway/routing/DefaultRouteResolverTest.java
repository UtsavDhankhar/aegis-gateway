package com.aegis.gateway.routing;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayRequest;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import com.aegis.gateway.routing.routeLocator.RouteLocator;
import com.aegis.gateway.routing.routeResolver.DefaultRouteResolver;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.aegis.gateway.routing.predicate.RoutePredicateFactory.method;
import static com.aegis.gateway.routing.predicate.RoutePredicateFactory.path;

class DefaultRouteResolverTest {

    @Test
    void shouldResolveLowestOrderMatchingRoute() {

        RouteDefinition genericRoute = RouteDefinition.builder()
                .id("generic-user-route")
                .targetUri("http://localhost:9001")
                .order(100)
                .predicates(List.of(
                        path("/gateway/users/**"),
                        method(HttpMethod.GET)
                ))
                .build();

        RouteDefinition adminRoute = RouteDefinition.builder()
                .id("admin-user-route")
                .targetUri("http://localhost:9003")
                .order(10)
                .predicates(List.of(
                        path("/gateway/users/admin/**"),
                        method(HttpMethod.GET)
                ))
                .build();

        RouteLocator locator = () -> Flux.just(genericRoute, adminRoute);

        DefaultRouteResolver resolver = new DefaultRouteResolver(locator);

        GatewayContext context = contextFor("http://localhost:8080/gateway/users/admin/roles", HttpMethod.GET);

        Mono<RouteDefinition> result = resolver.resolve(context);

        StepVerifier.create(result).expectNextMatches(route ->
                        route.getId().equals("admin-user-route") && route.getTargetUri().toString().equals("http://localhost:9003")
                ).verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenNoRouteMatches() {

        RouteDefinition route = RouteDefinition.builder()
                .id("user-route")
                .targetUri("http://localhost:9001")
                .order(10)
                .predicates(List.of(
                        path("/gateway/users/**"),
                        method(HttpMethod.GET)
                ))
                .build();

        RouteLocator locator = () -> Flux.just(route);

        DefaultRouteResolver resolver = new DefaultRouteResolver(locator);

        GatewayContext context = contextFor("http://localhost:8080/gateway/orders/123", HttpMethod.GET);

        StepVerifier.create(resolver.resolve(context)).verifyComplete();
    }

    private GatewayContext contextFor(String uri, HttpMethod method) {
        URI parsedUri = URI.create(uri);

        GatewayRequest request = new GatewayRequest(
                "request-1",
                method,
                parsedUri,
                parsedUri.getRawPath(),
                new HttpHeaders(),
                new LinkedMultiValueMap<>(),
                Optional.empty(),
                Instant.now(),
                Flux.empty()
        );

        return new GatewayContext(request);
    }
}
