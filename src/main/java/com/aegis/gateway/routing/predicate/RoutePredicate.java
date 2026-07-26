package com.aegis.gateway.routing.predicate;

import com.aegis.gateway.context.GatewayContext;

public interface RoutePredicate {

    String name();
    boolean matches(GatewayContext context);
}
