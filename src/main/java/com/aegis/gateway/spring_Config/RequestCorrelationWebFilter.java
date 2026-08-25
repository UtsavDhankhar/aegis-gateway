package com.aegis.gateway.spring_Config;

import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class RequestCorrelationWebFilter implements WebFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {

        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        String finalRequestId = requestId;

        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .headers(headers -> headers.set(REQUEST_ID_HEADER, finalRequestId))
                .build();

        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, finalRequestId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}
