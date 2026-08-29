package com.aegis.gateway.health;

import com.aegis.gateway.discovery.ServiceDiscoveryClient;
import com.aegis.gateway.health.InstanceHealthChecker.InstanceHealthChecker;
import com.aegis.gateway.loadbalancer.ServiceInstance;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class ActiveInstanceHealthMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveInstanceHealthMonitor.class);


    private final ServiceDiscoveryClient discoveryClient;
    private final InstanceHealthChecker instanceHealthChecker;
    private final InstanceHealthRegistry instanceHealthRegistry;
    private final HealthCheckProperties healthCheckProperties;

    private volatile Disposable subscription;

    public ActiveInstanceHealthMonitor(ServiceDiscoveryClient discoveryClient, InstanceHealthChecker instanceHealthChecker, InstanceHealthRegistry instanceHealthRegistry, HealthCheckProperties healthCheckProperties) {
        this.discoveryClient = discoveryClient;
        this.instanceHealthChecker = instanceHealthChecker;
        this.instanceHealthRegistry = instanceHealthRegistry;
        this.healthCheckProperties = healthCheckProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {

        if (!healthCheckProperties.enabled()) return;

        subscription = Flux.interval(Duration.ZERO, healthCheckProperties.interval())
                .concatMap(tick -> runHealthCheckCycle())
                .subscribe();
    }


    private Flux<Void> runHealthCheckCycle() {

        return discoveryClient.getServiceIds()
                .flatMap(discoveryClient::getInstances)
                .filter(ServiceInstance::enabled)
                .flatMap(this::checkInstance, 32);
    }

    private Mono<Void> checkInstance(ServiceInstance instance) {

        return instanceHealthChecker.isHealthy(instance)
                .doOnNext(healthy -> {
                    if (healthy) {
                        instanceHealthRegistry.recordSuccess(instance, healthCheckProperties.healthyThreshold());
                    } else {
                        instanceHealthRegistry.recordFailure(instance, healthCheckProperties.unhealthyThreshold());
                    }
                })
                .then();
    }

    @PreDestroy
    public void stop() {
        Disposable current = subscription;
        if (current != null) {
            current.dispose();
        }
    }
}