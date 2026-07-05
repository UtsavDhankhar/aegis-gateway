package com.aegis.gateway.pipeline;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class RequestIdGatewayFilter implements GatewayFilter {

    public static final String REQUEST_ID_ATTRIBUTE = "requestId";

    @Override
    public int order() {
        return 0;
    }

    @Override
    public Mono<GatewayResponse> filter(
            GatewayContext context,
            GatewayFilterChain chain
    ) {
        context.putAttribute(REQUEST_ID_ATTRIBUTE, context.request().requestId());
        return chain.next(context);
    }
}