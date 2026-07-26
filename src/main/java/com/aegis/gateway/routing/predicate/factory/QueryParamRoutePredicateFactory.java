package com.aegis.gateway.routing.predicate.factory;

import com.aegis.gateway.routing.predicate.RoutePredicate;
import com.aegis.gateway.routing.predicate.QueryParamRoutePredicate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public final class QueryParamRoutePredicateFactory implements RoutePredicateFactory {

    @Override
    public String name() {
        return "Query";
    }

    @Override
    public RoutePredicate create(Map<String, Object> args) {

        String paramName = PredicateArguments.requiredString(args, "name", name());
        String paramValue = PredicateArguments.requiredString(args, "value", name());

        return new QueryParamRoutePredicate(Map.of(paramName, paramValue));
    }
}