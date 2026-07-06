package com.aegis.gateway.routing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class RouteResolutionIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldResolveUserServiceRoute() {
        webTestClient.get()
                .uri("/gateway/users/123")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.message").isEqualTo(
                        "Matched route user-service-route -> http://localhost:9001"
                );
    }

    @Test
    void shouldResolveOrderServiceRoute() {
        webTestClient.post()
                .uri("/gateway/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.message").isEqualTo(
                        "Matched route order-service-route -> http://localhost:9002"
                );
    }

    @Test
    void shouldReturnNotFoundWhenNoRouteMatches() {
        webTestClient.get()
                .uri("/gateway/payments/123")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("No route matched");
    }

    @Test
    void shouldReturnNotFoundWhenMethodDoesNotMatch() {
        webTestClient.delete()
                .uri("/gateway/orders/123")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("No route matched");
    }
}