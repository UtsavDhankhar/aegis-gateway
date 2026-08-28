package com.aegis.gateway.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "aegis.gateway")
public record GatewayServicesProperties(
        List<@Valid ServiceConfig> services
) {


    public GatewayServicesProperties {
        services = services == null ? List.of() : List.copyOf(services);
    }

    public record ServiceConfig(

            @NotBlank String id,
            List<@Valid Instance> instances

    ) {

        public ServiceConfig {
            instances = instances == null ? List.of() : List.copyOf(instances);
        }
    }

    public record Instance(
            @NotBlank String id,
            @NotBlank String uri,
            Integer weight,
            Boolean enabled,
            Map<String, Object> metadata

    ){
        public Instance {
            weight = weight == null ? 1 : weight;
            enabled = enabled == null ? true : enabled;
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);

        }
    }

}
