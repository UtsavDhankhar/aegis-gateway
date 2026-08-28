package com.aegis.gateway.loadbalancer.balancer;

import com.aegis.gateway.loadbalancer.ServiceInstance;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RoundRobinBalancer implements LoadBalancer {

    private final ConcurrentHashMap<String, AtomicInteger> concurrentHashMap;

    public RoundRobinBalancer() {
        concurrentHashMap = new ConcurrentHashMap<>();
    }


    @Override
    public String getName() {
        return "round-robin";
    }

    @Override
    public Mono<ServiceInstance> choose(String serviceId, List<ServiceInstance> instances) {

        if (instances == null || instances.isEmpty()) return Mono.empty();

        AtomicInteger counter = concurrentHashMap.computeIfAbsent(serviceId, k -> new AtomicInteger(0));

        int idx = counter.getAndUpdate(val -> (val+1)%instances.size());

        return Mono.just(instances.get(idx));

    }
}
