package com.aegis.gateway.context;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public record GatewayRequest(
        String requestId,
        HttpMethod method,
        URI uri,
        String path,
        HttpHeaders headers,
        MultiValueMap<String, String> queryParams,
        Optional<InetSocketAddress> remoteAddress,
        Instant receivedAt,
        Flux<DataBuffer> body
) {
}
