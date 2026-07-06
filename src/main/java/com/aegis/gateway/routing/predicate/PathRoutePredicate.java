package com.aegis.gateway.routing.predicate;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.routing.RoutePredicate;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;
import java.util.List;

public final class PathRoutePredicate implements RoutePredicate {

    private final List<PathPattern> patterns;

    public PathRoutePredicate(String... patterns) {
        this(Arrays.asList(patterns));
    }

    public PathRoutePredicate(List<String> patterns) {
        PathPatternParser parser = PathPatternParser.defaultInstance;

        this.patterns = patterns.stream()
                .map(parser::parse)
                .toList();

        if (this.patterns.isEmpty()) {
            throw new IllegalArgumentException("path predicate requires at least one pattern");
        }
    }

    @Override
    public String name() {
        return "Path";
    }

    @Override
    public boolean matches(GatewayContext context) {
        PathContainer requestPath = PathContainer.parsePath(context.request().path());

        return patterns.stream()
                .anyMatch(pattern -> pattern.matches(requestPath));
    }
}