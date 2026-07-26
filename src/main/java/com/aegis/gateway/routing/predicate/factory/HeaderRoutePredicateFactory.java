package com.aegis.gateway.routing.predicate.factory;

import com.aegis.gateway.routing.predicate.RoutePredicate;
import com.aegis.gateway.routing.predicate.HeaderRoutePredicate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public final class HeaderRoutePredicateFactory implements RoutePredicateFactory {

    @Override
    public String name() {
        return "Header";
    }

    @Override
    public RoutePredicate create(Map<String, Object> args) {

        String headerName = PredicateArguments.requiredString(args, "name", name());
        String headerValue = PredicateArguments.requiredString(args, "value", name());

        return new HeaderRoutePredicate(Map.of(headerName, headerValue));
    }
}