package com.aegis.gateway.context;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class GatewayContext {

    private final GatewayRequest request;
    private final Instant startedAt;
    private final Map<String, Object> attributes;

    public GatewayContext(GatewayRequest request) {
        this.request = request;
        this.startedAt = Instant.now();
        this.attributes = new HashMap<>();
    }

    public GatewayRequest request() {
        return request;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Duration elapsedTime() {
        return Duration.between(startedAt, Instant.now());
    }

    public void putAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    public <T> Optional<T> getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);

        if (value == null) {
            return Optional.empty();
        }

        if (!type.isInstance(value)) {
            throw new IllegalStateException(
                    "Attribute '%s' is not of expected type %s"
                            .formatted(key, type.getName())
            );
        }

        return Optional.of(type.cast(value));
    }
}