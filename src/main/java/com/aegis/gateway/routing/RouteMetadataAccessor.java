package com.aegis.gateway.routing;

import com.aegis.gateway.routing.enums.RouteMetadataKeys;
import com.aegis.gateway.routing.exceptions.InvalidRouteMetadataException;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public final class RouteMetadataAccessor {

    public Optional<String> getString(RouteDefinition route, RouteMetadataKeys key) {

        Object value = route.getMetadata().get(key.getVal());

        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof String stringValue) {
            if (stringValue.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(stringValue);
        }

        throw new InvalidRouteMetadataException("Route '%s' metadata key '%s' must be a String".formatted(route.getId(), key));
    }

    public boolean getBoolean(RouteDefinition route, RouteMetadataKeys key, boolean defaultValue) {

        Object value = route.getMetadata().get(key.getVal());

        return switch (value) {
            case null -> defaultValue;
            case Boolean booleanValue -> booleanValue;
            case String stringValue -> Boolean.parseBoolean(stringValue);
            default ->
                    throw new InvalidRouteMetadataException("Route '%s' metadata key '%s' must be a Boolean".formatted(route.getId(), key));
        };

    }
}