package com.aegis.gateway.routing;


import com.aegis.gateway.config.GatewayRoutesProperties.PredicateConfig;
import com.aegis.gateway.config.GatewayRoutesProperties.RouteConfig;
import com.aegis.gateway.routing.exceptions.InvalidRouteConfigurationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                10,
                List.of(),
                Map.of()
        );

        assertThatThrownBy(() -> validator.validate(List.of(route)))
                .isInstanceOf(InvalidRouteConfigurationException.class)
                .hasMessageContaining("must contain at least one predicate");
    }

    private RouteConfig route(String id, String targetUri) {
        return new RouteConfig(
                id,
                targetUri,
                10,
                List.of(new PredicateConfig("Path", Map.of("patterns", List.of("/gateway/users/**")))),
                Map.of()
        );
    }
}