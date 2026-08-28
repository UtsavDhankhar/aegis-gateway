package com.aegis.gateway.proxy;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.loadbalancer.ServiceInstance;
import com.aegis.gateway.routing.RouteTarget;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import org.springframework.stereotype.Component;

import java.net.URI;

import static com.aegis.gateway.context.GatewayContextAttributes.SELECTED_SERVICE_INSTANCE;

@Component
public final class UpstreamTargetResolver {

    public URI resolve(GatewayContext context, RouteDefinition route) {

        return switch (route.getTarget()) {

            case RouteTarget.Direct direct -> direct.baseUri();

            case RouteTarget.Service service ->
                    context.getAttribute(SELECTED_SERVICE_INSTANCE, ServiceInstance.class)
                            .map(ServiceInstance::baseUri)
                            .orElseThrow(() ->
                                    new IllegalStateException("No selected instance for service: " + service.serviceId()));
        };
    }
}