package com.aegis.gateway.engine;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.pipeline.GatewayPipeline;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public final class GatewayHttpHandler {

    private final GatewayContextFactory contextFactory;
    private final GatewayPipeline gatewayPipeline;

    public GatewayHttpHandler(
            GatewayContextFactory contextFactory,
            GatewayPipeline gatewayPipeline
    ) {
        this.contextFactory = contextFactory;
        this.gatewayPipeline = gatewayPipeline;
    }

    public Mono<ServerResponse> handle(ServerRequest request) {
        GatewayContext context = contextFactory.create(request);

        return gatewayPipeline.execute(context)
                .flatMap(this::toServerResponse);
    }

    private Mono<ServerResponse> toServerResponse(GatewayResponse response) {
        return ServerResponse
                .status(response.status())
                .headers(headers -> headers.addAll(response.headers()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "%s",
                          "message": "%s"
                        }
                        """.formatted(
                        response.status().value(),
                        response.body()
                ));
    }
}