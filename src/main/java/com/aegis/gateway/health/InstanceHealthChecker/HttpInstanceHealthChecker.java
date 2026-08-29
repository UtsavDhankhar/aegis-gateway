package com.aegis.gateway.health.InstanceHealthChecker;

import com.aegis.gateway.health.HealthCheckProperties;
import com.aegis.gateway.loadbalancer.ServiceInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class HttpInstanceHealthChecker implements InstanceHealthChecker {


    private static final String HEALTH_PATH_METADATA = "aegis.health.path";

    private final WebClient webClient;
    private final HealthCheckProperties healthCheckProperties;

    public HttpInstanceHealthChecker(@Qualifier("healthCheckWebClient") WebClient healthWebClient,
                                     HealthCheckProperties healthCheckProperties) {

        this.webClient = healthWebClient;
        this.healthCheckProperties = healthCheckProperties;
    }

    @Override
    public Mono<Boolean> isHealthy(ServiceInstance instance) {

        URI healthUri = buildHealthUri(instance);

        return webClient.get()
                .uri(healthUri)
                .exchangeToMono(response -> {
                    boolean healthy = response.statusCode().is2xxSuccessful();
                    return response.releaseBody().thenReturn(healthy);
                })
                .timeout(healthCheckProperties.timeout())
                .onErrorReturn(false);
    }


    private URI buildHealthUri(ServiceInstance instance) {


        String healthPath = String.valueOf(instance.metadata().getOrDefault(
                                        HEALTH_PATH_METADATA,
                                        healthCheckProperties.defaultPath()
                                )
                );

        return UriComponentsBuilder
                .fromUri(instance.baseUri())
                .replacePath(healthPath)
                .replaceQuery(null)
                .build()
                .toUri();
    }
}
