package com.aegis.gateway.health;

import com.aegis.gateway.loadbalancer.ServiceInstance;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class InstanceHealthRegistry {

    private final Map<ServiceInstanceKey, InstanceHealthState> instanceHealthStateMap = new ConcurrentHashMap<>();

    public InstanceHealthState getInstanceHealthState(ServiceInstance instance) {

        return instanceHealthStateMap.getOrDefault(getServiceInstanceKey(instance),
                InstanceHealthState.unknown());
    }

    public void recordSuccess(ServiceInstance instance, int healthyThreshold) {

        instanceHealthStateMap.compute(
                getServiceInstanceKey(instance), (key, previous) -> {

                    InstanceHealthState current = previous == null
                                    ? InstanceHealthState.unknown()
                                    : previous;

                    int successes = current.consecutiveSuccesses() + 1;

                    InstanceHealthStatus status = successes >= healthyThreshold
                                    ? InstanceHealthStatus.HEALTHY
                                    : current.status();

                    return new InstanceHealthState(
                            status,
                            successes,
                            0,
                            Instant.now()
                    );
                }
        );
    }

    public void recordFailure(ServiceInstance instance, int unhealthyThreshold) {

        instanceHealthStateMap.compute(
                getServiceInstanceKey(instance), (key, previous) -> {

                    InstanceHealthState current = previous == null
                                    ? InstanceHealthState.unknown()
                                    : previous;

                    int failures = current.consecutiveFailures() + 1;

                    InstanceHealthStatus status = failures >= unhealthyThreshold
                                    ? InstanceHealthStatus.UNHEALTHY
                                    : current.status();

                    return new InstanceHealthState(
                            status,
                            0,
                            failures,
                            Instant.now()
                    );
                }
        );
    }

    private ServiceInstanceKey getServiceInstanceKey(ServiceInstance instance) {

        return new ServiceInstanceKey(
                instance.serviceId(),
                instance.instanceId()
        );
    }
}