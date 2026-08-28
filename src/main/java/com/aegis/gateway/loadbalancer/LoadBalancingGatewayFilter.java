package com.aegis.gateway.loadbalancer;

import com.aegis.gateway.context.GatewayContext;
import com.aegis.gateway.context.GatewayResponse;
import com.aegis.gateway.loadbalancer.balancer.LoadBalancer;
import com.aegis.gateway.loadbalancer.instancesupplier.ServiceInstanceSupplier;
import com.aegis.gateway.pipeline.GatewayFilter;
import com.aegis.gateway.pipeline.GatewayFilterChain;
import com.aegis.gateway.routing.RouteMetadataAccessor;
import com.aegis.gateway.routing.RouteTarget;
import com.aegis.gateway.routing.routeDefination.RouteDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.aegis.gateway.context.GatewayContextAttributes.SELECTED_ROUTE;
import static com.aegis.gateway.context.GatewayContextAttributes.SELECTED_SERVICE_INSTANCE;
import static com.aegis.gateway.loadbalancer.LoadBalancerMetadataKeys.DEFAULT_LOAD_BALANCER_NAME;
import static com.aegis.gateway.loadbalancer.LoadBalancerMetadataKeys.LOAD_BALANCER_NAME;

@Component
public class LoadBalancingGatewayFilter implements GatewayFilter {

    private final ServiceInstanceSupplier serviceInstanceSupplier;
    private final LoadBalancerRegistry loadBalancerRegistry;
    private final RouteMetadataAccessor metadataAccessor;

    public LoadBalancingGatewayFilter(ServiceInstanceSupplier serviceInstanceSupplier, LoadBalancerRegistry loadBalancerRegistry, RouteMetadataAccessor metadataAccessor) {
        this.serviceInstanceSupplier = serviceInstanceSupplier;
        this.loadBalancerRegistry = loadBalancerRegistry;
        this.metadataAccessor = metadataAccessor;
    }


    @Override
    public int order() {
        return 1000;
    }

    @Override
    public Mono<GatewayResponse> filter(GatewayContext gatewayContext, GatewayFilterChain chain) {

        Optional<RouteDefinition> routeDefinitionOptional = gatewayContext.getAttribute(SELECTED_ROUTE, RouteDefinition.class);

        if (routeDefinitionOptional.isEmpty()) {
            return Mono.just(GatewayResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LoadBalancer route definition not found"));
        }

        RouteDefinition routeDefinition = routeDefinitionOptional.get();

        if (routeDefinition.getTarget() instanceof RouteTarget.Direct) return chain.next(gatewayContext);

        RouteTarget.Service target = (RouteTarget.Service) routeDefinition.getTarget();

        String balancerName = metadataAccessor.getString(routeDefinition, LOAD_BALANCER_NAME).orElse(DEFAULT_LOAD_BALANCER_NAME);

        LoadBalancer loadBalancer = loadBalancerRegistry.getLoadBalancer(balancerName);

        return serviceInstanceSupplier.getInstances(target.serviceId())
                .collectList()
                .flatMap(serviceInstances -> {

                    if (serviceInstances.isEmpty()) {
                        return noInstanceResponse(target.serviceId());
                    }


                    return loadBalancer.choose(target.serviceId(), serviceInstances)
                            .flatMap(serviceInstance -> {
                                gatewayContext.putAttribute(SELECTED_SERVICE_INSTANCE, serviceInstance);

                                return chain.next(gatewayContext);
                            })
                            .switchIfEmpty(noInstanceResponse(target.serviceId()));

                });
    }


    private Mono<GatewayResponse> noInstanceResponse(String serviceId) {

            return Mono.just(GatewayResponse.error(HttpStatus.SERVICE_UNAVAILABLE, "No available instance for service: " + serviceId));
        }
}
