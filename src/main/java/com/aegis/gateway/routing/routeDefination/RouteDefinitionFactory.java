package com.aegis.gateway.routing.routeDefination;

import com.aegis.gateway.config.GatewayRoutesProperties.RouteConfig;
import com.aegis.gateway.routing.predicate.RoutePredicate;
import com.aegis.gateway.routing.predicate.factory.RoutePredicateFactoryRegistry;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
public final class RouteDefinitionFactory {

    private final RoutePredicateFactoryRegistry predicateFactoryRegistry;

    public RouteDefinitionFactory(RoutePredicateFactoryRegistry predicateFactoryRegistry) {
        this.predicateFactoryRegistry = predicateFactoryRegistry;
    }

    public RouteDefinition create(RouteConfig routeConfig) {
        List<RoutePredicate> predicates = routeConfig.predicates()
                .stream()
                .map(predicateFactoryRegistry::create)
                .toList();

        RouteDefinition.Builder builder =  RouteDefinition.builder()
                .id(routeConfig.id())
                .order(routeConfig.order())
                .predicates(predicates)
                .metadata(routeConfig.metadata());


        if (routeConfig.serviceId() != null && !routeConfig.serviceId().isEmpty()) {
            builder.serviceId(routeConfig.serviceId());
        } else {
            builder.targetUri(routeConfig.targetUri());
        }

        return builder.build();

    }
}