package com.aegis.gateway.security;

import reactor.core.publisher.Mono;

public interface JwtAuthenticationService {

    Mono<AuthenticatedPrincipal> authenticate(String token);
}
