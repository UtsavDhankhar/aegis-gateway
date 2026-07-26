package com.aegis.gateway.routing.predicate;

import com.aegis.gateway.context.GatewayContext;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.Set;

public final class MethodRoutePredicate implements RoutePredicate {

    private final Set<HttpMethod> methods;

    public MethodRoutePredicate(HttpMethod... methods) {
        this.methods = Set.copyOf(Arrays.asList(methods));

        if (this.methods.isEmpty()) {
            throw new IllegalArgumentException("method predicate requires at least one HTTP method");
        }
    }

    @Override
    public String name() {
        return "Method";
    }

    @Override
    public boolean matches(GatewayContext context) {
        return methods.contains(context.request().method());
    }
}