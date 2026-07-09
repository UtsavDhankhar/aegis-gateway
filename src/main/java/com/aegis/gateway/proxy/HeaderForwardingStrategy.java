package com.aegis.gateway.proxy;

import com.aegis.gateway.context.GatewayRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public final class HeaderForwardingStrategy {

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of("connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te", "trailer",
            "transfer-encoding",
            "upgrade",
            "host",
            "content-length");

    public void copyRequestHeaders(GatewayRequest request, HttpHeaders targetHeaders) {
        Set<String> connectionHeaderValues = extractConnectionHeaderValues(request.headers());

        request.headers().forEach((name, values) -> {
            if (shouldSkipHeader(name, connectionHeaderValues)) {
                return;
            }

            targetHeaders.addAll(name, values);
        });

        targetHeaders.set("X-Request-Id", request.requestId());
        targetHeaders.set("X-Forwarded-Host", originalHost(request));
        targetHeaders.set("X-Forwarded-Proto", request.uri().getScheme());

        request.remoteAddress()
                .map(InetSocketAddress::getHostString)
                .ifPresent(ip -> targetHeaders.add("X-Forwarded-For", ip));
    }

    public HttpHeaders copyResponseHeaders(HttpHeaders sourceHeaders) {
        HttpHeaders targetHeaders = new HttpHeaders();

        Set<String> connectionHeaderValues = extractConnectionHeaderValues(sourceHeaders);

        sourceHeaders.forEach((name, values) -> {
            if (shouldSkipHeader(name, connectionHeaderValues)) {
                return;
            }

            targetHeaders.addAll(name, values);
        });

        return targetHeaders;
    }

    private boolean shouldSkipHeader(String headerName, Set<String> connectionHeaderValues) {
        String normalized = headerName.toLowerCase(Locale.ROOT);

        return HOP_BY_HOP_HEADERS.contains(normalized) || connectionHeaderValues.contains(normalized);
    }

    private Set<String> extractConnectionHeaderValues(HttpHeaders headers) {
        Set<String> values = new HashSet<>();

        headers.getOrEmpty(HttpHeaders.CONNECTION).forEach(value -> {
            String[] tokens = value.split(",");
            for (String token : tokens) {
                String normalized = token.trim().toLowerCase(Locale.ROOT);

                if (!normalized.isBlank()) {
                    values.add(normalized);
                }
            }
        });

        return values;
    }

    private String originalHost(GatewayRequest request) {
        InetSocketAddress host = request.headers().getHost();

        if (host == null) {
            return request.uri().getHost();
        }

        if (host.getPort() == -1) {
            return host.getHostString();
        }

        return host.getHostString() + ":" + host.getPort();
    }
}