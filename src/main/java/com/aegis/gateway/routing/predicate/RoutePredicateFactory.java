package com.aegis.gateway.routing.predicate;

import com.aegis.gateway.routing.RoutePredicate;
import org.springframework.http.HttpMethod;

import java.util.Map;

public final class RoutePredicateFactory {

    private RoutePredicateFactory() {
    }

    public static RoutePredicate path(String... patterns) {
        return new PathRoutePredicate(patterns);
    }

    public static RoutePredicate method(HttpMethod... methods) {
        return new MethodRoutePredicate(methods);
    }

    public static RoutePredicate host(String... hostPatterns) {
        return new HostRoutePredicate(hostPatterns);
    }

    public static RoutePredicate header(String name, String value) {
        return new HeaderRoutePredicate(Map.of(name, value));
    }

    public static RoutePredicate query(String name, String value) {
        return new QueryParamRoutePredicate(Map.of(name, value));
    }
}