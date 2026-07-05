package com.aegis.gateway.engine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class GatewayEntryPointConfig {

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes(GatewayHttpHandler gatewayHttpHandler) {
        return route(path("/gateway/**"), gatewayHttpHandler::handle);
    }
}