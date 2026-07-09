package com.aegis.gateway.proxy;

import com.aegis.gateway.context.GatewayRequest;
import com.aegis.gateway.routing.RouteDefinition;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class BackendUriBuilder {

    public URI buildBackendUri(RouteDefinition routeDefinition,
                               GatewayRequest gatewayRequest) {

        return UriComponentsBuilder
                .fromUri(routeDefinition.getTargetUri())
                .replacePath(gatewayRequest.uri().getRawPath())
                .replaceQuery(gatewayRequest.uri().getRawQuery())
                .build(true)
                .toUri();
    }
}
