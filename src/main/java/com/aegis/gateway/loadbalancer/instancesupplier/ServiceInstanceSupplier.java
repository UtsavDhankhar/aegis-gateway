package com.aegis.gateway.loadbalancer.instancesupplier;

import com.aegis.gateway.loadbalancer.ServiceInstance;
import reactor.core.publisher.Flux;

public interface ServiceInstanceSupplier {

    Flux<ServiceInstance> getInstances(String serviceId);
}
