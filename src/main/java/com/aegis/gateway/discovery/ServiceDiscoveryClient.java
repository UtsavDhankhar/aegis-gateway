package com.aegis.gateway.discovery;

import com.aegis.gateway.loadbalancer.ServiceInstance;
import reactor.core.publisher.Flux;

public interface ServiceDiscoveryClient {

    Flux<String> getServiceIds();

    Flux<ServiceInstance> getInstances(String serviceId);
}