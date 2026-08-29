package com.aegis.gateway.loadbalancer.instancesupplier;

import com.aegis.gateway.config.GatewayServicesProperties;
import com.aegis.gateway.loadbalancer.ServiceInstance;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


//@Component // TODO: OLD FLOW, without Health Check.
public class ConfigServiceInstanceSupplier implements ServiceInstanceSupplier{

    private final Map<String, List<ServiceInstance>> instances;

    public ConfigServiceInstanceSupplier(GatewayServicesProperties gatewayServicesProperties) {

        instances = gatewayServicesProperties.services()
                .stream()
                .collect(Collectors.toUnmodifiableMap(GatewayServicesProperties.ServiceConfig::id, this::toServiceInstances));

    }


    @Override
    public Flux<ServiceInstance> getInstances(String serviceId) {
        return Flux.fromIterable(instances.getOrDefault(serviceId, List.of()));
    }


    private List<ServiceInstance> toServiceInstances(GatewayServicesProperties.ServiceConfig serviceConfig) {

        return serviceConfig.instances().stream()
                .map(instance -> new ServiceInstance(
                        instance.id(),
                        serviceConfig.id(),
                        URI.create(instance.uri()),
                        instance.weight(),
                        instance.enabled(),
                        instance.metadata()
                ))
                .toList();
    }
}
