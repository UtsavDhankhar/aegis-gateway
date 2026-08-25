package com.aegis.gateway.observability;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayRequest;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.pipeline.GatewayFilterChain;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class GatewayObservabilityFilterTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private final GatewayObservabilityFilter filter = new GatewayObservabilityFilter(meterRegistry);

    @Test
    void shouldRecordGatewayMetric() {

        GatewayContext context = createContext();

        GatewayFilterChain chain = ctx -> Mono.just(GatewayResponse.ok("done"));

        StepVerifier.create(filter.filter(context, chain)).expectNextCount(1).verifyComplete();

        Timer timer = meterRegistry.find("aegis.gateway.requests").tag("method", "GET").tag("status", "200").timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }


    private GatewayContext createContext() {

        URI uri = URI.create(
                "http://localhost:8080/gateway/users/123"
        );

        GatewayRequest request = new GatewayRequest(
                "test-request-1",
                HttpMethod.GET,
                uri,
                uri.getRawPath(),
                new HttpHeaders(),
                new LinkedMultiValueMap<>(),
                Optional.empty(),
                Instant.now(),
                Flux.empty()
        );

        return new GatewayContext(request);
    }
}
