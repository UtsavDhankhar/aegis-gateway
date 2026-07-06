package com.aegis.gateway.pipeline;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.aegis.gateway.context.GatewayContextAttributes.REQUEST_ID;

@Component
public final class RequestIdGatewayFilter implements GatewayFilter {

    @Override
    public int order() {
        return 0;
    }

    @Override
    public Mono<GatewayResponse> filter(GatewayContext context,GatewayFilterChain chain) {
        context.putAttribute(REQUEST_ID, context.request().requestId());
        return chain.next(context);
    }
}