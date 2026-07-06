package com.aegis.gateway.routing.predicate;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.routing.RoutePredicate;
import org.springframework.http.HttpHeaders;

import java.util.Map;

public final class HeaderRoutePredicate implements RoutePredicate {

    private final Map<String, String> requiredHeaders;

    public HeaderRoutePredicate(Map<String, String> requiredHeaders) {
        this.requiredHeaders = Map.copyOf(requiredHeaders);

        if (this.requiredHeaders.isEmpty()) {
            throw new IllegalArgumentException("header predicate requires at least one header");
        }
    }

    @Override
    public String name() {
        return "Header";
    }

    @Override
    public boolean matches(GatewayContext context) {
        HttpHeaders headers = context.request().headers();

        return requiredHeaders.entrySet()
                .stream()
                .allMatch(entry -> {
                    String actualValue = headers.getFirst(entry.getKey());
                    return entry.getValue().equals(actualValue);
                });
    }
}