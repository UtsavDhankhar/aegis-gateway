package com.aegis.gateway.routing.predicate;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.routing.RoutePredicate;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public final class QueryParamRoutePredicate implements RoutePredicate {

    private final Map<String, String> requiredParams;

    public QueryParamRoutePredicate(Map<String, String> requiredParams) {
        this.requiredParams = Map.copyOf(requiredParams);

        if (this.requiredParams.isEmpty()) {
            throw new IllegalArgumentException("query param predicate requires at least one parameter");
        }
    }

    @Override
    public String name() {
        return "Query";
    }

    @Override
    public boolean matches(GatewayContext context) {
        MultiValueMap<String, String> queryParams = context.request().queryParams();

        return requiredParams.entrySet()
                .stream()
                .allMatch(entry -> {
                    String actualValue = queryParams.getFirst(entry.getKey());
                    return entry.getValue().equals(actualValue);
                });
    }
}