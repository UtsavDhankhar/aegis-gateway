package com.aegis.gateway.loadbalancer.balancer;

import com.aegis.gateway.loadbalancer.ServiceInstance;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LoadBalancer {

    String getName();
    Mono<ServiceInstance> choose(String serviceId, List<ServiceInstance> instances);
}
