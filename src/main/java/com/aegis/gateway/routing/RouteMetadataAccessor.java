package com.aegis.gateway.routing;

import com.aegis.gateway.routing.exceptions.InvalidRouteMetadataException;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public final class RouteMetadataAccessor {

    public Optional<String> getString(RouteDefinition route, String key) {

        Object value = route.getMetadata().get(key);

        if (value == null) {

            String[] parts = key.split("\\.");
            Object metadata = route.getMetadata();

            for (String part : parts) {

                if (!(metadata instanceof Map<?,?> map)) {
                    return Optional.empty();
                }

                metadata = map.get(part);
                if (metadata == null) {return Optional.empty();}
            }
            value = metadata;
        }

        if (value instanceof String stringValue) {
            if (stringValue.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(stringValue);
        }

        throw new InvalidRouteMetadataException("Route '%s' metadata key '%s' must be a String".formatted(route.getId(), key));
    }

    public boolean getBoolean(RouteDefinition route, String key, boolean defaultValue) {

        Object value = route.getMetadata().get(key);

        return switch (value) {
            case null -> defaultValue;
            case Boolean booleanValue -> booleanValue;
            case String stringValue -> Boolean.parseBoolean(stringValue);
            default ->
                    throw new InvalidRouteMetadataException("Route '%s' metadata key '%s' must be a Boolean".formatted(route.getId(), key));
        };

    }

    public List<String> getStringList(RouteDefinition route, String key) {

        Object value = route.getMetadata().get(key);

        if (value == null) {
            return List.of();
        }

        if (value instanceof String stringValue) {
            String normalized = stringValue.trim();

            return normalized.isBlank() ? List.of() : List.of(normalized);
        }

        if (value instanceof Collection<?> collection) {
            return normalizeValues(collection);
        }

        if (value instanceof Map<?, ?> map) {
            return normalizeValues(map.values());
        }

        throw new InvalidRouteMetadataException("Route '%s' metadata key '%s' must be a String or list. Actual type: %s"
                .formatted(
                        route.getId(),
                        key,
                        value.getClass().getName()
                )
        );
    }


    public long getLong(RouteDefinition route, String key, long defaultValue) {

        Object value = route.getMetadata().get(key);

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException exception) {
                throw new InvalidRouteMetadataException(
                        "Route '%s' metadata '%s' must be numeric".formatted(route.getId(), key), exception
                );
            }
        }

        throw new InvalidRouteMetadataException(
                "Route '%s' metadata '%s' must be numeric".formatted(route.getId(), key)
        );
    }


    private List<String> normalizeValues(Collection<?> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}