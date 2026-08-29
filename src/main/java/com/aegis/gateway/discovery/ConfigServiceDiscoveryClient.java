package com.aegis.gateway.discovery;

import com.aegis.gateway.config.GatewayServicesProperties;
import com.aegis.gateway.loadbalancer.ServiceInstance;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public final class ConfigServiceDiscoveryClient implements ServiceDiscoveryClient {

    private final Map<String, List<ServiceInstance>> instances;

    public ConfigServiceDiscoveryClient(GatewayServicesProperties properties) {

        this.instances = properties.services().stream()
                .collect(Collectors.toUnmodifiableMap(GatewayServicesProperties.ServiceConfig::id, this::toInstances));
    }

    @Override
    public Flux<String> getServiceIds() {
        return Flux.fromIterable(instances.keySet());
    }

    @Override
    public Flux<ServiceInstance> getInstances(String serviceId) {

        return Flux.fromIterable(instances.getOrDefault(serviceId, List.of()));
    }

    private List<ServiceInstance> toInstances(GatewayServicesProperties.ServiceConfig service) {

        return service.instances().stream()
                .map(instance -> new ServiceInstance(instance.id(),
                        service.id(),
                        URI.create(instance.uri()),
                        instance.weight(),
                        instance.enabled(),
                        instance.metadata()))
                .toList();
    }
}