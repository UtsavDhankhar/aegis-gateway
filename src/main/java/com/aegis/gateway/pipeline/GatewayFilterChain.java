package com.aegis.gateway.pipeline;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import reactor.core.publisher.Mono;

public interface GatewayFilterChain {

    Mono<GatewayResponse> next(GatewayContext context);
}
