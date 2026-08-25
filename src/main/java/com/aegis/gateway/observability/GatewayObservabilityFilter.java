package com.aegis.gateway.observability;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.pipeline.GatewayFilter;
import com.aegis.gateway.pipeline.GatewayFilterChain;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.aegis.gateway.context.GatewayContextAttributes.SELECTED_ROUTE;

@Component
public final class GatewayObservabilityFilter implements GatewayFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayObservabilityFilter.class);

    private static final String UNKNOWN_ROUTE = "UNMATCHED";

    private final MeterRegistry meterRegistry;

    public GatewayObservabilityFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public int order() {
        return 50;
    }

    @Override
    public Mono<GatewayResponse> filter(GatewayContext context, GatewayFilterChain chain) {

        Timer.Sample sample = Timer.start(meterRegistry);

        return chain.next(context)
                .doOnNext(response -> recordSuccessfulCompletion(context, response, sample))
                .doOnError(error -> recordUnhandledFailure(context, error, sample));
    }

    private void recordSuccessfulCompletion(GatewayContext context, GatewayResponse response, Timer.Sample sample) {

        int status = GatewayResponseStatus.statusCode(response);

        String routeId = resolveRouteId(context);

        GatewayOutcome outcome = GatewayOutcome.fromStatus(status);

        Timer timer = Timer.builder("aegis.gateway.requests")
                .description("Aegis gateway request execution time")
                .tag("route", routeId)
                .tag("method", context.request().method().name())
                .tag("status", Integer.toString(status))
                .tag("outcome", outcome.name())
                .publishPercentileHistogram()
                .register(meterRegistry);

        sample.stop(timer);

        logCompletedRequest(context, routeId, status, outcome);
    }

    private void recordUnhandledFailure(GatewayContext context, Throwable error, Timer.Sample sample) {

        String routeId = resolveRouteId(context);

        Timer timer = Timer.builder("aegis.gateway.requests")
                .description("Aegis gateway request execution time")
                .tag("route", routeId)
                .tag("method", context.request().method().name())
                .tag("status", "EXCEPTION")
                .tag("outcome", GatewayOutcome.SERVER_ERROR.name())
                .publishPercentileHistogram()
                .register(meterRegistry);

        sample.stop(timer);

        LOGGER.atError()
                .addKeyValue("requestId", context.request().requestId())
                .addKeyValue("method", context.request().method().name())
                .addKeyValue("path", context.request().path())
                .addKeyValue("routeId", routeId)
                .addKeyValue("errorType", error.getClass().getSimpleName())
                .setCause(error).log("gateway.request.failed");
    }

    private void logCompletedRequest(GatewayContext context, String routeId, int status, GatewayOutcome outcome) {

        LOGGER.atInfo().addKeyValue("requestId", context.request().requestId())
                .addKeyValue("method", context.request().method().name())
                .addKeyValue("path", context.request().path())
                .addKeyValue("routeId", routeId).addKeyValue("status", status)
                .addKeyValue("outcome", outcome.name())
                .addKeyValue("durationMs", context.elapsedTime().toMillis())
                .log("gateway.request.completed");
    }

    private String resolveRouteId(GatewayContext context) {

        Optional<RouteDefinition> route = context.getAttribute(SELECTED_ROUTE, RouteDefinition.class);

        return route.map(RouteDefinition::getId).orElse(UNKNOWN_ROUTE);
    }
}
