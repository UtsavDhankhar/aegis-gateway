package com.aegis.gateway.routing;

import com.aegis.gateway.context.GatewayContext;

public interface RoutePredicate {

    String name();
    boolean matches(GatewayContext context);
}
