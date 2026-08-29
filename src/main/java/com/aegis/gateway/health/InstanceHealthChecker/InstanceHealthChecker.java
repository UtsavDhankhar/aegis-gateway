package com.aegis.gateway.health.InstanceHealthChecker;

import com.aegis.gateway.loadbalancer.ServiceInstance;
import reactor.core.publisher.Mono;

public interface InstanceHealthChecker {

    Mono<Boolean> isHealthy(ServiceInstance instance);

}
