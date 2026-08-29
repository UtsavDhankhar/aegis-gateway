package com.aegis.gateway.loadbalancer.instancesupplier;

import com.aegis.gateway.discovery.ServiceDiscoveryClient;
import com.aegis.gateway.health.HealthCheckProperties;
import com.aegis.gateway.health.InstanceHealthRegistry;
import com.aegis.gateway.health.InstanceHealthStatus;
import com.aegis.gateway.loadbalancer.ServiceInstance;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public final class HealthAwareServiceInstanceSupplier implements ServiceInstanceSupplier {

    private final ServiceDiscoveryClient discoveryClient;
    private final InstanceHealthRegistry healthRegistry;
    private final HealthCheckProperties healthProperties;

    public HealthAwareServiceInstanceSupplier(ServiceDiscoveryClient discoveryClient,
                                              InstanceHealthRegistry healthRegistry,
                                              HealthCheckProperties healthProperties) {
        this.discoveryClient = discoveryClient;
        this.healthRegistry = healthRegistry;
        this.healthProperties = healthProperties;
    }

    @Override
    public Flux<ServiceInstance> getInstances(String serviceId) {

        return discoveryClient.getInstances(serviceId)
                .filter(ServiceInstance::enabled)
                .filter(this::isEligible);
    }

    private boolean isEligible(ServiceInstance instance) {

        if (!healthProperties.enabled()) {
            return true;
        }

        InstanceHealthStatus status = healthRegistry.getInstanceHealthState(instance).status();
        return status != InstanceHealthStatus.UNHEALTHY;
    }
}