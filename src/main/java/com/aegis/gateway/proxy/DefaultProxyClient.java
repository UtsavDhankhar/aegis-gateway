package com.aegis.gateway.proxy;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayRequest;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import com.aegis.gateway.routing.exceptions.InvalidRouteMetadataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Component
public final class DefaultProxyClient implements ProxyClient {


    private static final Logger LOGGER =
            LoggerFactory.getLogger(DefaultProxyClient.class);

    private static final Set<HttpMethod> METHODS_THAT_MAY_HAVE_BODY = Set.of(HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.PATCH);

    private final WebClient proxyWebClient;
    private final BackendUriBuilder backendUriBuilder;
    private final HeaderForwardingStrategy headerForwardingStrategy;
    private final UpstreamTargetResolver upstreamTargetResolver;

    public DefaultProxyClient(WebClient proxyWebClient, BackendUriBuilder backendUriBuilder,
                              HeaderForwardingStrategy headerForwardingStrategy, UpstreamTargetResolver upstreamTargetResolver) {
        this.proxyWebClient = proxyWebClient;
        this.backendUriBuilder = backendUriBuilder;
        this.headerForwardingStrategy = headerForwardingStrategy;
        this.upstreamTargetResolver = upstreamTargetResolver;
    }

    @Override
    public Mono<GatewayResponse> proxy(GatewayContext context, RouteDefinition route) {

        return Mono.defer(() -> {

                    GatewayRequest request = context.request();
                    URI upstreamBaseUrl = upstreamTargetResolver.resolve(context, route);
                    URI backendUri = backendUriBuilder.buildBackendUri(route, request, upstreamBaseUrl);

                    WebClient.RequestBodySpec requestSpec = proxyWebClient.method(request.method())
                            .uri(backendUri)
                            .headers(headers -> headerForwardingStrategy.copyRequestHeaders(request, headers));


                    if (METHODS_THAT_MAY_HAVE_BODY.contains(request.method())) {
                        return requestSpec
                                .body(BodyInserters.fromDataBuffers(request.body()))
                                .exchangeToMono(this::toGatewayResponse);
                    }

                    return requestSpec.exchangeToMono(this::toGatewayResponse);

                }).onErrorResume(InvalidRouteMetadataException.class,
                exception -> Mono.just(GatewayResponse.error(INTERNAL_SERVER_ERROR,
                        "Gateway route metadata error: " + exception.getMessage())))
                .onErrorResume(throwable -> {

                    LOGGER.error(
                            "Proxy request failed. requestId={}, routeId={}",
                            context.request().requestId(),
                            route.getId(),
                            throwable);

                    return Mono.just(GatewayResponse.error(BAD_GATEWAY,
                            "Bad gateway: " + throwable.getClass().getSimpleName()));

                });
    }


    private Mono<GatewayResponse> toGatewayResponse(ClientResponse clientResponse) {
        HttpHeaders responseHeaders =
                headerForwardingStrategy.copyResponseHeaders(
                        clientResponse.headers().asHttpHeaders()
                );

        return clientResponse.bodyToMono(byte[].class)
                .defaultIfEmpty(new byte[0])
                .map(body -> new GatewayResponse.SimpleGatewayResponse(
                        clientResponse.statusCode(),
                        responseHeaders,
                        body
                ));
    }


}