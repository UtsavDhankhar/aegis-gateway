package com.aegis.gateway.security;

import com.aegis.gateway.security.config.GatewaySecurityProperties;
import com.aegis.gateway.security.exceptions.InvalidBearerTokenException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public final class JwtPrincipalMapper {

    private final GatewaySecurityProperties properties;

    public JwtPrincipalMapper(GatewaySecurityProperties properties) {
        this.properties = properties;
    }

    public AuthenticatedPrincipal map(Jwt jwt) {
        String subject = extractSubject(jwt);

        Set<String> roles = extractValues(jwt.getClaims().get(properties.rolesClaim())).stream()
                .map(this::normalizeRole)
                .collect(Collectors.toUnmodifiableSet());

        Set<String> scopes = extractValues(jwt.getClaims().get(properties.scopesClaim()));

        return new AuthenticatedPrincipal(
                subject,
                roles,
                scopes,
                jwt.getClaims()
        );
    }

    private String extractSubject(Jwt jwt) {
        Object claim = jwt.getClaims().get(properties.subjectClaim());

        if (claim == null || claim.toString().isBlank()) {
            throw new InvalidBearerTokenException("JWT does not contain required subject claim: " + properties.subjectClaim());
        }

        return claim.toString().trim();
    }

    private Set<String> extractValues(Object claimValue) {
        if (claimValue == null) {
            return Set.of();
        }

        if (claimValue instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toUnmodifiableSet());
        }

        if (claimValue instanceof String stringValue) {
            return Arrays.stream(stringValue.split("[,\\s]+"))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toUnmodifiableSet());
        }

        return new LinkedHashSet<>(
                Set.of(claimValue.toString().trim())
        );
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);

        String rolePrefix = properties.rolePrefix().toUpperCase(Locale.ROOT);

        if (!rolePrefix.isBlank() && normalized.startsWith(rolePrefix)) {
            normalized = normalized.substring(rolePrefix.length());
        }

        return normalized;
    }
}