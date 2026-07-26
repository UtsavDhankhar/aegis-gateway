package com.aegis.gateway.routing.predicate.factory;

import com.aegis.gateway.routing.predicate.RoutePredicate;
import com.aegis.gateway.routing.predicate.PathRoutePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public final class PathRoutePredicateFactory implements RoutePredicateFactory {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    public String name() {
        return "Path";
    }

    @Override
    public RoutePredicate create(Map<String, Object> args) {

        Object patterns = args.get("patterns");
        LOGGER.info("Path patterns value = {}, type = {}", patterns, patterns == null ? null : patterns.getClass());

        return new PathRoutePredicate(PredicateArguments.requiredStringList(args, "patterns", name()));
    }
}