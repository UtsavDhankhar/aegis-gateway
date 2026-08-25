package com.aegis.gateway.security;

import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public final class SpringJwtAuthenticationService implements JwtAuthenticationService {

    private final ReactiveJwtDecoder jwtDecoder;
    private final JwtPrincipalMapper principalMapper;

    public SpringJwtAuthenticationService(ReactiveJwtDecoder jwtDecoder, JwtPrincipalMapper principalMapper) {
        this.jwtDecoder = jwtDecoder;
        this.principalMapper = principalMapper;
    }

    @Override
    public Mono<AuthenticatedPrincipal> authenticate(String token) {
        return jwtDecoder
                .decode(token)
                .map(principalMapper::map);
    }
}