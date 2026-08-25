package com.aegis.gateway.security;

import com.aegis.gateway.pipeline.GatewayFilter;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.pipeline.GatewayFilterChain;
import com.aegis.gateway.routing.RouteMetadataAccessor;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.aegis.gateway.context.GatewayContextAttributes.AUTHENTICATED_PRINCIPAL;
import static com.aegis.gateway.context.GatewayContextAttributes.SELECTED_ROUTE;
import static com.aegis.gateway.security.SecurityMetadataKeys.ALLOWED_ROLES;

@Component
public final class AuthorizationGatewayFilter implements GatewayFilter {

    private final RouteMetadataAccessor metadataAccessor;

    public AuthorizationGatewayFilter(RouteMetadataAccessor metadataAccessor) {
        this.metadataAccessor = metadataAccessor;
    }

    @Override
    public int order() {
        return 300;
    }

    @Override
    public Mono<GatewayResponse> filter(GatewayContext context, GatewayFilterChain chain) {

        Optional<RouteDefinition> routeOptional = context.getAttribute(SELECTED_ROUTE,
                RouteDefinition.class);

        if (routeOptional.isEmpty()) {
            return Mono.just(GatewayResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Authorization filter reached without selected route"));
        }

        RouteDefinition route = routeOptional.get();

        List<String> configuredRoles = metadataAccessor.getStringList(route, ALLOWED_ROLES);

        if (configuredRoles.isEmpty()) {
            return chain.next(context);
        }

        Optional<AuthenticatedPrincipal> principalOptional = context.getAttribute(AUTHENTICATED_PRINCIPAL,
                AuthenticatedPrincipal.class);

        if (principalOptional.isEmpty()) {
            return Mono.just(GatewayResponse.error(HttpStatus.UNAUTHORIZED,
                    "Authentication is required for this route"));
        }

        AuthenticatedPrincipal principal = principalOptional.get();

        Set<String> allowedRoles = configuredRoles.stream()
                .map(this::normalizeRole)
                .collect(Collectors.toUnmodifiableSet());

        boolean authorized = principal.roles().stream()
                .map(this::normalizeRole)
                .anyMatch(allowedRoles::contains);

        if (!authorized) {
            return Mono.just(GatewayResponse.error(HttpStatus.FORBIDDEN,
                    "Insufficient role for this route"));
        }

        return chain.next(context);
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        return normalized;
    }
}