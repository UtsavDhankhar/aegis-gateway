package com.aegis.gateway.proxy;

import com.aegis.gateway.context.GatewayRequest;
import com.aegis.gateway.proxy.pathRewrite.DefaultPathRewrite;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import com.aegis.gateway.routing.RouteMetadataAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.aegis.gateway.routing.enums.RouteMetadataKeys.PREFIX_PATH;
import static com.aegis.gateway.routing.enums.RouteMetadataKeys.REWRITE_PATH_REGEX;
import static com.aegis.gateway.routing.enums.RouteMetadataKeys.REWRITE_PATH_REPLACEMENT;
import static com.aegis.gateway.routing.enums.RouteMetadataKeys.STRIP_PREFIX;
import static com.aegis.gateway.routing.predicate.RoutePredicateFactory.path;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultPathRewriteStrategyTest {

    private final DefaultPathRewrite strategy = new DefaultPathRewrite(new RouteMetadataAccessor());

    @Test
    void shouldStripPrefix() {
        RouteDefinition route = routeWithMetadata(Map.of(STRIP_PREFIX.getVal(), "/gateway"));

        GatewayRequest request = request("/gateway/users/123");
        String rewrittenPath = strategy.rewritePath(route, request);
        assertThat(rewrittenPath).isEqualTo("/users/123");
    }

    @Test
    void shouldNotStripPartialSegmentPrefix() {
        RouteDefinition route = routeWithMetadata(Map.of(STRIP_PREFIX.getVal(), "/gateway"));

        GatewayRequest request = request("/gateway-v2/users/123");
        String rewrittenPath = strategy.rewritePath(route, request);
        assertThat(rewrittenPath).isEqualTo("/gateway-v2/users/123");
    }

    @Test
    void shouldReturnRootWhenPathEqualsPrefix() {
        RouteDefinition route = routeWithMetadata(Map.of(STRIP_PREFIX.getVal(), "/gateway"));

        GatewayRequest request = request("/gateway");
        String rewrittenPath = strategy.rewritePath(route, request);
        assertThat(rewrittenPath).isEqualTo("/");
    }

    @Test
    void shouldPrefixPath() {
        RouteDefinition route = routeWithMetadata(Map.of(PREFIX_PATH.getVal(), "/internal"));

        GatewayRequest request = request("/users/123");
        String rewrittenPath = strategy.rewritePath(route, request);
        assertThat(rewrittenPath).isEqualTo("/internal/users/123");
    }

    @Test
    void shouldRewriteUsingRegex() {
        RouteDefinition route = routeWithMetadata(Map.of(
                REWRITE_PATH_REGEX.getVal(), "/gateway/users/(?<id>.*)",
                REWRITE_PATH_REPLACEMENT.getVal(), "/users/${id}"
        ));

        GatewayRequest request = request("/gateway/users/123");
        String rewrittenPath = strategy.rewritePath(route, request);
        assertThat(rewrittenPath).isEqualTo("/users/123");
    }

    private RouteDefinition routeWithMetadata(Map<String, Object> metadata) {
        return RouteDefinition.builder()
                .id("test-route")
                .targetUri("http://localhost:9001")
                .order(1)
                .predicates(List.of(path("/**")))
                .metadata(metadata)
                .build();
    }

    private GatewayRequest request(String path) {
        return new GatewayRequest(
                "request-1",
                HttpMethod.GET,
                URI.create("http://localhost:8080" + path),
                path,
                new HttpHeaders(),
                new LinkedMultiValueMap<>(),
                Optional.empty(),
                Instant.now(),
                Flux.empty()
        );
    }
}