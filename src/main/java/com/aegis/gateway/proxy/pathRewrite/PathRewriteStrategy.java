package com.aegis.gateway.proxy.pathRewrite;

import com.aegis.gateway.context.GatewayRequest;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;

public interface PathRewriteStrategy {

    String rewritePath(RouteDefinition route, GatewayRequest request);
}