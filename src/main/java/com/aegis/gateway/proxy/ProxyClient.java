package com.aegis.gateway.proxy;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.routing.RouteDefinition;
import reactor.core.publisher.Mono;

public interface ProxyClient {

    Mono<GatewayResponse> proxy(GatewayContext context, RouteDefinition route);
}