package com.aegis.gateway.loadbalancer.balancer;

import com.aegis.gateway.loadbalancer.ServiceInstance;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RandomBalancer implements LoadBalancer {
    @Override
    public String getName() {
        return "random";
    }

    @Override
    public Mono<ServiceInstance> choose(String serviceId, List<ServiceInstance> instances) {

        if  (instances == null || instances.isEmpty()) return Mono.empty();

        int index = ThreadLocalRandom.current().nextInt(instances.size());

        return Mono.just(instances.get(index));
    }
}
