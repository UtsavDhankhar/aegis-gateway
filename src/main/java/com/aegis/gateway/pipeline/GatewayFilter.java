package com.aegis.gateway.pipeline;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import reactor.core.publisher.Mono;

public interface GatewayFilter {

    int order();

    Mono<GatewayResponse> filter(
            GatewayContext gatewayContext,
            GatewayFilterChain chain
    );
}
