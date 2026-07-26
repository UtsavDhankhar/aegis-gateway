package com.aegis.gateway.routing;

import com.aegis.gateway.config.GatewayRoutesProperties.RouteConfig;
import com.aegis.gateway.routing.exceptions.InvalidRouteConfigurationException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public final class RouteConfigValidator {

    private static final Set<String> SUPPORTED_TARGET_SCHEMES = Set.of("http", "https");

    public void validate(List<RouteConfig> routes) {
        Set<String> routeIds = new HashSet<>();

        for (RouteConfig route : routes) {
            validateRouteId(route, routeIds);
            validateTargetUri(route);
            validatePredicates(route);
        }
    }

    private void validateRouteId(RouteConfig route, Set<String> routeIds) {
        if (!routeIds.add(route.id())) {
            throw new InvalidRouteConfigurationException("Duplicate route id: " + route.id());
        }
    }

    private void validateTargetUri(RouteConfig route) {
        URI uri;

        try {
            uri = URI.create(route.targetUri());
        } catch (IllegalArgumentException exception) {
            throw new InvalidRouteConfigurationException("Route '%s' has invalid target URI '%s'".formatted(route.id(), route.targetUri()), exception);
        }

        if (uri.getScheme() == null || !SUPPORTED_TARGET_SCHEMES.contains(uri.getScheme())) {
            throw new InvalidRouteConfigurationException("Route '%s' target URI must use http or https: %s".formatted(route.id(), route.targetUri()));
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new InvalidRouteConfigurationException("Route '%s' target URI must contain host: %s".formatted(route.id(), route.targetUri()));
        }
    }

    private void validatePredicates(RouteConfig route) {
        if (route.predicates().isEmpty()) {
            throw new InvalidRouteConfigurationException("Route '%s' must contain at least one predicate".formatted(route.id()));
        }
    }
}