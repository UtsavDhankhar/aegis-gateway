package com.aegis.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "aegis.gateway")
public record GatewayRoutesProperties(@Valid List<RouteConfig> routes) {

    public GatewayRoutesProperties {
        routes = routes == null ? List.of() : List.copyOf(routes);
    }

    public record RouteConfig(
            @NotBlank String id,
            @NotBlank String targetUri,
            Integer order,
            @Valid List<PredicateConfig> predicates,
            Map<String, Object> metadata
    ) {
        public RouteConfig {
            order = order == null ? 0 : order;
            predicates = predicates == null ? List.of() : List.copyOf(predicates);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    public record PredicateConfig(
            @NotBlank String name,
            Map<String, Object> args
    ) {
        public PredicateConfig {
            args = args == null ? Map.of() : Map.copyOf(args);
        }
    }
}