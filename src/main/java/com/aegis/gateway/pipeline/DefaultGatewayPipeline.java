package com.aegis.gateway.pipeline;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Component
public final class DefaultGatewayPipeline implements GatewayPipeline {

    private final List<GatewayFilter> filters;

    public DefaultGatewayPipeline(List<GatewayFilter> filters) {
        this.filters = filters.stream()
                .sorted(Comparator.comparingInt(GatewayFilter::order))
                .toList();
    }

    @Override
    public Mono<GatewayResponse> execute(GatewayContext context) {
        return new DefaultGatewayFilterChain(filters, 0)
                .next(context)
                .switchIfEmpty(Mono.just(
                        GatewayResponse.error(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Gateway pipeline completed without response"
                        )
                ));
    }
}