package com.aegis.gateway.routing;

import com.aegis.gateway.context.GatewayContext;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RouteDefinition {

    private final String id;
    private final URI targetUri;
    private final int order;
    private final List<RoutePredicate> predicates;
    private final Map<String, Object> metadata;

    private RouteDefinition(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "route id must not be null");
        this.targetUri = Objects.requireNonNull(builder.targetUri, "target uri must not be null");
        this.order = builder.order;
        this.predicates = List.copyOf(builder.predicates);
        this.metadata = Map.copyOf(builder.metadata);

        if (this.predicates.isEmpty()) {
            throw new IllegalArgumentException("route must contain at least one predicate");
        }
    }

    public String getId() {
        return id;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public int getOrder() {
        return order;
    }

    public List<RoutePredicate> getPredicateList() {
        return predicates;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean matches(GatewayContext context) {
        return predicates.stream()
                .allMatch(predicate -> predicate.matches(context));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private URI targetUri;
        private int order = 0;
        private List<RoutePredicate> predicates = List.of();
        private Map<String, Object> metadata = new HashMap<>();

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder targetUri(String targetUri) {
            this.targetUri = URI.create(targetUri);
            return this;
        }

        public Builder targetUri(URI targetUri) {
            this.targetUri = targetUri;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder predicates(List<RoutePredicate> predicates) {
            this.predicates = List.copyOf(predicates);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = new HashMap<>(metadata);
            return this;
        }

        public RouteDefinition build() {
            return new RouteDefinition(this);
        }
    }
}