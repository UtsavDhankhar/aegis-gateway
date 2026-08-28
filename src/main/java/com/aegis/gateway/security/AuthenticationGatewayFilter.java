package com.aegis.gateway.security;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.pipeline.GatewayFilter;
import com.aegis.gateway.pipeline.GatewayFilterChain;
import com.aegis.gateway.routing.RouteMetadataAccessor;
import com.aegis.gateway.routing.enums.RouteMetadataKeys;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import com.aegis.gateway.security.exceptions.InvalidBearerTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.aegis.gateway.context.GatewayContextAttributes.AUTHENTICATED_PRINCIPAL;
import static com.aegis.gateway.context.GatewayContextAttributes.SELECTED_ROUTE;
import static com.aegis.gateway.security.SecurityMetadataKeys.AUTHENTICATION_REQUIRED;

@Component
public final class AuthenticationGatewayFilter implements GatewayFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AuthenticationGatewayFilter.class);

    private final RouteMetadataAccessor metadataAccessor;
    private final BearerTokenExtractor tokenExtractor;
    private final JwtAuthenticationService authenticationService;

    public AuthenticationGatewayFilter(RouteMetadataAccessor metadataAccessor,
            BearerTokenExtractor tokenExtractor,
            JwtAuthenticationService authenticationService
    ) {
        this.metadataAccessor = metadataAccessor;
        this.tokenExtractor = tokenExtractor;
        this.authenticationService = authenticationService;
    }

    @Override
    public int order() {
        return 200;
    }

    @Override
    public Mono<GatewayResponse> filter(GatewayContext context, GatewayFilterChain chain) {
        Optional<RouteDefinition> routeOptional =
                context.getAttribute(
                        SELECTED_ROUTE,
                        RouteDefinition.class
                );

        if (routeOptional.isEmpty()) {
            return Mono.just(GatewayResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Authentication filter reached without selected route"));
        }

        RouteDefinition route = routeOptional.get();

        boolean authenticationRequired = metadataAccessor.getBoolean(route, RouteMetadataKeys.AUTHENTICATION_REQUIRED.getVal(), false);

        if (!authenticationRequired) {
            return chain.next(context);
        }

        final Optional<String> tokenOptional;

        try {
            tokenOptional = tokenExtractor.extract(context.request());
        } catch (InvalidBearerTokenException exception) {
            return unauthorized("Malformed bearer token");
        }

        if (tokenOptional.isEmpty()) {
            return unauthorized("Bearer token is required");
        }

        return authenticationService
                .authenticate(tokenOptional.get())
                .flatMap(principal -> {
                    context.putAttribute(AUTHENTICATED_PRINCIPAL, principal);
                    return chain.next(context);
                })
                .onErrorResume(
                        JwtException.class,
                        exception -> {
                            LOGGER.debug("JWT validation failed for requestId={}",
                                    context.request().requestId(), exception);

                            return unauthorized("Invalid or expired bearer token");
                        }
                )
                .onErrorResume(InvalidBearerTokenException.class,
                        exception -> unauthorized("Invalid bearer token claims")
                );
    }

    private Mono<GatewayResponse> unauthorized(String message) {
        return Mono.just(GatewayResponse.error(HttpStatus.UNAUTHORIZED, message)
        );
    }
}
