package com.aegis.gateway.security;

import java.util.Map;
import java.util.Set;

public record AuthenticatedPrincipal(
        String subject,
        Set<String> roles,
        Set<String> scopes,
        Map<String, Object> claims
) {

    public AuthenticatedPrincipal {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
        claims = claims == null ? Map.of() : Map.copyOf(claims);
    }
}