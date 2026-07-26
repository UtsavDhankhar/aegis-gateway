package com.aegis.gateway.routing.predicate.factory;

import com.aegis.gateway.routing.predicate.RoutePredicate;

import java.util.Map;

public interface RoutePredicateFactory {

    String name();

    RoutePredicate create(Map<String, Object> args);
}
