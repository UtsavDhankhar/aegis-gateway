package com.aegis.gateway.security;

import com.aegis.gateway.context.GatewayRequest;
import com.aegis.gateway.security.exceptions.InvalidBearerTokenException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public final class BearerTokenExtractor {

    private static final String BEARER_SCHEME = "Bearer";

    public Optional<String> extract(GatewayRequest request) {
        String authorization = request.headers().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || authorization.isBlank()) {
            return Optional.empty();
        }

        String[] parts = authorization.trim().split("\\s+", 2);

        if (parts.length != 2) {
            throw new InvalidBearerTokenException("Authorization header must use Bearer token format");
        }

        if (!BEARER_SCHEME.equalsIgnoreCase(parts[0])) {
            throw new InvalidBearerTokenException("Unsupported Authorization scheme");
        }

        String token = parts[1].trim();

        if (token.isBlank()) {
            throw new InvalidBearerTokenException("Bearer token must not be empty");
        }

        return Optional.of(token);
    }
}
