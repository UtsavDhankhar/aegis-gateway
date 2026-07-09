package com.aegis.gateway.proxy;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.pipeline.GatewayFilter;
import com.aegis.gateway.pipeline.GatewayFilterChain;
import com.aegis.gateway.routing.RouteDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.aegis.gateway.context.GatewayContextAttributes.SELECTED_ROUTE;

@Component
public final class ProxyGatewayFilter implements GatewayFilter {

    private final ProxyClient proxyClient;

    public ProxyGatewayFilter(ProxyClient proxyClient) {
        this.proxyClient = proxyClient;
    }

    @Override
    public int order() {
        return 5_000;
    }

    @Override
    public Mono<GatewayResponse> filter(GatewayContext context, GatewayFilterChain chain) {

        return context.getAttribute(SELECTED_ROUTE, RouteDefinition.class)
                .map(route -> proxyClient.proxy(context, route))
                .orElseGet(() -> Mono.just(
                        GatewayResponse.error(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Proxy filter reached without selected route"
                        )
                ));
    }
}