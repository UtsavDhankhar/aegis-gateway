package com.aegis.gateway.pipeline;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import reactor.core.publisher.Mono;

import java.util.List;

final class DefaultGatewayFilterChain implements GatewayFilterChain {

    private final List<GatewayFilter> filters;
    private final int index;

    DefaultGatewayFilterChain(List<GatewayFilter> filters, int index) {
        this.filters = filters;
        this.index = index;
    }

    @Override
    public Mono<GatewayResponse> next(GatewayContext context) {
        if (index >= filters.size()) {
            return Mono.just(GatewayResponse.ok("Aegis Gateway pipeline executed"));
        }

        GatewayFilter currentFilter = filters.get(index);
        GatewayFilterChain nextChain = new DefaultGatewayFilterChain(filters, index + 1);

        return currentFilter.filter(context, nextChain);
    }
}