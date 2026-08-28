package com.aegis.gateway.loadbalancer;

import com.aegis.gateway.loadbalancer.balancer.LoadBalancer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public final class LoadBalancerRegistry {

    private final Map<String, LoadBalancer> loadBalancers;

    public LoadBalancerRegistry(List<LoadBalancer> loadBalancers) {
        this.loadBalancers = loadBalancers.stream()
                .collect(Collectors.toUnmodifiableMap(loadBalancer -> normalize(loadBalancer.getName()),
                        Function.identity()));
    }


    public LoadBalancer getLoadBalancer(String name) {
        LoadBalancer loadBalancer = loadBalancers.get(normalize(name));

        if (loadBalancer == null) {
            throw new IllegalArgumentException("LoadBalancer with name " + name + " not found");
        }

        return loadBalancer;
    }


    private String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
