package com.aegis.gateway.health;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class HealthCheckClientConfig {

    @Bean
    public WebClient healthCheckWebClient(HealthCheckProperties properties) {

        HttpClient httpClient = HttpClient.create().responseTimeout(properties.timeout());

        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }
}
