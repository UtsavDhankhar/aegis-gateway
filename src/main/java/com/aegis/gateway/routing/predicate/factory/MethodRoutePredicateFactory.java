package com.aegis.gateway.routing.predicate.factory;

import com.aegis.gateway.routing.predicate.RoutePredicate;
import com.aegis.gateway.routing.exceptions.InvalidRouteConfigurationException;
import com.aegis.gateway.routing.predicate.MethodRoutePredicate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public final class MethodRoutePredicateFactory implements RoutePredicateFactory {

    @Override
    public String name() {
        return "Method";
    }

    @Override
    public RoutePredicate create(Map<String, Object> args) {
        List<HttpMethod> methods = PredicateArguments
                .requiredStringList(args, "methods", name())
                .stream()
                .map(this::parseMethod)
                .toList();

        return new MethodRoutePredicate(methods.toArray(HttpMethod[]::new));
    }

    private HttpMethod parseMethod(String method) {
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidRouteConfigurationException("Predicate '%s' has invalid HTTP method '%s'".formatted(name(), method), exception);
        }
    }
}