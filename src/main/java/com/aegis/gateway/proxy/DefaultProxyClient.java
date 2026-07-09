package com.aegis.gateway.proxy;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayRequest;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.routing.RouteDefinition;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

@Component
public final class DefaultProxyClient implements ProxyClient {

    private static final Set<HttpMethod> METHODS_THAT_MAY_HAVE_BODY = Set.of(HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.PATCH);

    private final WebClient proxyWebClient;
    private final BackendUriBuilder backendUriBuilder;
    private final HeaderForwardingStrategy headerForwardingStrategy;

    public DefaultProxyClient(WebClient proxyWebClient, BackendUriBuilder backendUriBuilder,
                              HeaderForwardingStrategy headerForwardingStrategy) {

        this.proxyWebClient = proxyWebClient;
        this.backendUriBuilder = backendUriBuilder;
        this.headerForwardingStrategy = headerForwardingStrategy;
    }

    @Override
    public Mono<GatewayResponse> proxy(GatewayContext context, RouteDefinition route) {

        GatewayRequest request = context.request();
        URI backendUri = backendUriBuilder.buildBackendUri(route, request);

        WebClient.RequestBodySpec requestSpec = proxyWebClient.method(request.method())
                .uri(backendUri)
                .headers(headers -> headerForwardingStrategy.copyRequestHeaders(request, headers));

        Mono<GatewayResponse> responseMono;

        if (METHODS_THAT_MAY_HAVE_BODY.contains(request.method())) {
            responseMono = requestSpec.body(BodyInserters.fromDataBuffers(request.body()))
                    .exchangeToMono(clientResponse -> toGatewayResponse(clientResponse.statusCode(),
                            clientResponse.headers().asHttpHeaders(),
                            clientResponse.bodyToFlux(DataBuffer.class)));
        } else {
            responseMono = requestSpec.exchangeToMono(clientResponse ->
                    toGatewayResponse(clientResponse.statusCode(),
                    clientResponse.headers().asHttpHeaders(),
                    clientResponse.bodyToFlux(DataBuffer.class)));
        }

        return responseMono.onErrorResume(throwable -> Mono.just(GatewayResponse.error(HttpStatus.BAD_GATEWAY,
                "Bad gateway: " + throwable.getClass().getSimpleName())));
    }

    private Mono<GatewayResponse> toGatewayResponse(HttpStatusCode statusCode, HttpHeaders backendHeaders,
                                                    Flux<DataBuffer> backendBody) {

        HttpHeaders responseHeaders = headerForwardingStrategy.copyResponseHeaders(backendHeaders);
        ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(statusCode);
        responseBuilder.headers(headers -> headers.addAll(responseHeaders));
        return responseBuilder.body(backendBody, DataBuffer.class).map(GatewayResponse::nativeResponse);
    }
}