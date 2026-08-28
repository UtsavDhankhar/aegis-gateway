package com.aegis.gateway.loadbalancer;


import com.aegis.gateway.loadbalancer.balancer.RoundRobinBalancer;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.Map;

class RoundRobinLoadBalancerTest {

    private final RoundRobinBalancer loadBalancer = new RoundRobinBalancer();

    @Test
    void shouldSelectInstancesInRoundRobinOrder() {

        ServiceInstance first = instance("user-1", 9001);
        ServiceInstance second = instance("user-2", 9003);
        List<ServiceInstance> instances = List.of(first, second);

        StepVerifier.create(loadBalancer.choose("user-service", instances)).expectNext(first).verifyComplete();

        StepVerifier.create(loadBalancer.choose("user-service", instances)).expectNext(second).verifyComplete();

        StepVerifier.create(loadBalancer.choose("user-service", instances)).expectNext(first).verifyComplete();
    }

    private ServiceInstance instance(String id, int port) {

        return new ServiceInstance(id, "user-service",
                URI.create("http://localhost:" + port), 1, true, Map.of());
    }
}