package com.aegis.gateway.routing.predicate.factory;

import com.aegis.gateway.config.GatewayRoutesProperties.PredicateConfig;
import com.aegis.gateway.routing.predicate.RoutePredicate;
import com.aegis.gateway.routing.exceptions.InvalidRouteConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public final class RoutePredicateFactoryRegistry {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final Map<String, RoutePredicateFactory> factories;

    public RoutePredicateFactoryRegistry(List<RoutePredicateFactory> predicateFactories) {

        Map<String, RoutePredicateFactory> registry = new HashMap<>();

        for (RoutePredicateFactory factory : predicateFactories) {
            String key = normalize(factory.name());
            if (registry.containsKey(key))
                throw new InvalidRouteConfigurationException("Duplicate route predicate factory registered: " + factory.name());
            registry.put(key, factory);
        }

        this.factories = Map.copyOf(registry);
    }

    public RoutePredicate create(PredicateConfig predicateConfig) {

        LOGGER.info("Creating predicate: {}, args = {}, args type = {}",
                predicateConfig.name(), predicateConfig.args(), predicateConfig.args().getClass());

        RoutePredicateFactory factory = factories.get(normalize(predicateConfig.name()));
        if (factory == null) throw new InvalidRouteConfigurationException("Unknown route predicate: " + predicateConfig.name());
        return factory.create(predicateConfig.args());
    }

    private String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}