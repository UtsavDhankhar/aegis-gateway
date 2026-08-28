package com.aegis.gateway.routing;


import com.aegis.gateway.config.GatewayRoutesProperties.PredicateConfig;
import com.aegis.gateway.config.GatewayRoutesProperties.RouteConfig;
import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayRequest;
import com.aegis.gateway.routing.exceptions.InvalidRouteConfigurationException;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import com.aegis.gateway.routing.routeLocator.RouteLocator;
import com.aegis.gateway.routing.routeResolver.DefaultRouteResolver;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.aegis.gateway.routing.predicate.RoutePredicateFactory.method;
import static com.aegis.gateway.routing.predicate.RoutePredicateFactory.path;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RouteConfigValidatorTest {

    private final RouteConfigValidator validator = new RouteConfigValidator();

    @Test
    void shouldRejectDuplicateRouteIds() {
        RouteConfig first = route("user-route", "http://localhost:9001");
        RouteConfig second = route("user-route", "http://localhost:9002");

        assertThatThrownBy(() -> validator.validate(List.of(first, second)))
                .isInstanceOf(InvalidRouteConfigurationException.class)
                .hasMessageContaining("Duplicate route id");
    }

    @Test
    void shouldRejectUnsupportedTargetScheme() {
        RouteConfig route = route("user-route", "ftp://localhost:9001");

        assertThatThrownBy(() -> validator.validate(List.of(route)))
                .isInstanceOf(InvalidRouteConfigurationException.class)
                .hasMessageContaining("http or https");
    }

    @Test
    void shouldRejectRouteWithoutPredicates() {

        RouteConfig route = new RouteConfig(
                "user-route",
                "http://localhost:9001",
                null,
                10,
                List.of(),
                Map.of()
        );

        assertThatThrownBy(() -> validator.validate(List.of(route)))
                .isInstanceOf(InvalidRouteConfigurationException.class)
                .hasMessageContaining("must contain at least one predicate");
    }

    @Test
    void shouldResolveRouteWithServiceTarget() {

        RouteDefinition userRoute = RouteDefinition.builder()
                .id("user-service-route")
                .serviceId("user-service")
                .order(10)
                .predicates(List.of(
                        path("/gateway/users/**"),
                        method(HttpMethod.GET)
                ))
                .build();

        RouteLocator locator = () -> Flux.just(userRoute);
        DefaultRouteResolver resolver = new DefaultRouteResolver(locator);
        GatewayContext context = contextFor(
                "http://localhost:8080/gateway/users/123",
                HttpMethod.GET
        );

        StepVerifier.create(resolver.resolve(context))
                .assertNext(route -> {
                    assertThat(route.getId()).isEqualTo("user-service-route");
                    assertThat(route.getTarget()).isInstanceOf(RouteTarget.Service.class);
                    RouteTarget.Service target = (RouteTarget.Service) route.getTarget();
                    assertThat(target.serviceId()).isEqualTo("user-service");
                })
                .verifyComplete();
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

    private RouteConfig route(String id, String targetUri) {
        return new RouteConfig(
                id,
                targetUri,
                null,
                10,
                List.of(new PredicateConfig("Path", Map.of("patterns", List.of("/gateway/users/**")))),
                Map.of()
        );
    }
}