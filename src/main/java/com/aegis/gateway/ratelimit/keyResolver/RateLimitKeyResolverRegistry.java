package com.aegis.gateway.ratelimit.keyResolver;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public final class RateLimitKeyResolverRegistry {

    private final Map<String, RateLimitKeyResolver> resolvers;

    public RateLimitKeyResolverRegistry(List<RateLimitKeyResolver> resolvers) {
        this.resolvers = resolvers.stream().
                collect(Collectors.toUnmodifiableMap(resolver -> normalize(resolver.getName()),
                        Function.identity()));
    }

    public RateLimitKeyResolver get(String name) {

        RateLimitKeyResolver resolver = resolvers.get(normalize(name));

        if (resolver == null) {
            throw new IllegalArgumentException("Unknown rate limit key resolver: " + name);
        }

        return resolver;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
