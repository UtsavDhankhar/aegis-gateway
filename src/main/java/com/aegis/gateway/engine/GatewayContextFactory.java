package com.aegis.gateway.engine;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public final class GatewayContextFactory {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    public GatewayContext create(ServerRequest request) {
        String requestId = request.headers()
                .firstHeader(REQUEST_ID_HEADER);

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        GatewayRequest gatewayRequest = new GatewayRequest(
                requestId,
                request.method(),
                request.uri(),
                request.path(),
                request.headers().asHttpHeaders(),
                request.queryParams(),
                Optional.ofNullable(request.remoteAddress().orElse(null)),
                Instant.now()
        );

        return new GatewayContext(gatewayRequest);
    }
}