package com.aegis.gateway.routing.routeLocator;

import com.aegis.gateway.config.GatewayRoutesProperties;
import com.aegis.gateway.routing.RouteConfigValidator;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import com.aegis.gateway.routing.routeDefination.RouteDefinitionFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class YamlRouteLocator implements RouteLocator{

    private final List<RouteDefinition> routes;

    public YamlRouteLocator(GatewayRoutesProperties properties, RouteConfigValidator validator, RouteDefinitionFactory routeDefinitionFactory) {

        validator.validate(properties.routes());
        this.routes = properties.routes()
                .stream()
                .map(routeDefinitionFactory::create)
                .toList();
    }

    @Override
    public Flux<RouteDefinition> getRoutes() {
        return Flux.fromIterable(routes);
    }
}
