package com.aegis.gateway.routing.predicate.factory;


import com.aegis.gateway.routing.predicate.RoutePredicate;
import com.aegis.gateway.routing.predicate.HostRoutePredicate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public final class HostRoutePredicateFactory implements RoutePredicateFactory {

    @Override
    public String name() {
        return "Host";
    }

    @Override
    public RoutePredicate create(Map<String, Object> args) {
        return new HostRoutePredicate(PredicateArguments.requiredStringList(args, "patterns", name()).toArray(String[]::new));
    }
}
