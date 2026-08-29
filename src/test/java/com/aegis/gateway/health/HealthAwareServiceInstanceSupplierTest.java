package com.aegis.gateway.health;

import com.aegis.gateway.discovery.ServiceDiscoveryClient;
import com.aegis.gateway.loadbalancer.ServiceInstance;
import com.aegis.gateway.loadbalancer.instancesupplier.HealthAwareServiceInstanceSupplier;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HealthAwareServiceInstanceSupplierTest {

    @Test
    void shouldMarkInstanceUnhealthyAfterThreshold() {

        InstanceHealthRegistry registry = new InstanceHealthRegistry();

        ServiceInstance instance = new ServiceInstance("user-1",
                "user-service",
                URI.create("http://localhost:9001"),
                1, true, Map.of());

        registry.recordFailure(instance, 2);

        assertThat(registry.getInstanceHealthState(instance).status()).isEqualTo(InstanceHealthStatus.UNKNOWN);

        registry.recordFailure(instance, 2);

        assertThat(registry.getInstanceHealthState(instance).status()).isEqualTo(InstanceHealthStatus.UNHEALTHY);
    }


    @Test
    void shouldExcludeUnhealthyInstance() {

        ServiceInstance healthy = instance("user-1", 9001);
        ServiceInstance unhealthy = instance("user-2", 9003);

        ServiceDiscoveryClient discovery = new ServiceDiscoveryClient() {

            @Override
            public Flux<String> getServiceIds() {
                return Flux.just("user-service");
            }

            @Override
            public Flux<ServiceInstance> getInstances(String serviceId) {
                return Flux.just(healthy, unhealthy);
            }
        };

        InstanceHealthRegistry registry = new InstanceHealthRegistry();

        registry.recordFailure(unhealthy, 1);

        HealthCheckProperties properties = new HealthCheckProperties(true, Duration.ofSeconds(5), Duration.ofSeconds(1), 1, 1, "/actuator/health/readiness");

        HealthAwareServiceInstanceSupplier supplier = new HealthAwareServiceInstanceSupplier(discovery, registry, properties);

        StepVerifier.create(supplier.getInstances("user-service")).expectNext(healthy).verifyComplete();
    }

    private ServiceInstance instance(String instanceId, int port) {

        return new ServiceInstance(
                instanceId,
                "user-service",
                URI.create("http://localhost:" + port),
                1,
                true,
                Map.of()
        );
    }
}
