package com.aegis.gateway.proxy;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Import(ProxyIntegrationTest.JwtTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ProxyIntegrationTest {

    private static final List<HttpServer> userServiceList = new ArrayList<>();

    @Autowired
    private WebTestClient webTestClient;


    @TestConfiguration
    static class JwtTestConfiguration {

        @Bean
        @Primary
        ReactiveJwtDecoder testJwtDecoder() {
            return token -> Mono.just(
                    Jwt.withTokenValue(token)
                            .header("alg", "RS256")
                            .subject("test-user")
                            .claim("roles", List.of("USER"))
                            .issuedAt(Instant.now())
                            .expiresAt(Instant.now().plusSeconds(300))
                            .build()
            );
        }
    }


    @BeforeAll
    static void startBackend() throws IOException {

        createHttpServer(9001);
        createHttpServer(9003);
    }

    private static void createHttpServer(Integer port) throws IOException {

        HttpServer userService = HttpServer.create(new InetSocketAddress(port), 0);
        userService.createContext("/users/123", exchange -> {
            byte[] response = """
                    {
                      "id": 123,
                      "name": "Amoeba",
                      "source": "user-service"
                    }
                    """.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });

        userService.createContext("/actuator/health/readiness", exchange -> {
            byte[] body = """
                    {"status" : "UP"}
                    """.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");

            exchange.sendResponseHeaders(200, body.length);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(body);
            }

        });

        userService.start();
        userServiceList.add(userService);
    }


    @AfterAll
    static void stopBackend() {
        if (!userServiceList.isEmpty()) {
            userServiceList.forEach(userService -> userService.stop(0));
        }
    }

    @Test
    void shouldProxyRequestToUserService() {
        webTestClient.get()
                .uri("/gateway/users/123")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(123)
                .jsonPath("$.name").isEqualTo("Amoeba")
                .jsonPath("$.source").isEqualTo("user-service");
    }


    @Test
    void shouldProxyAuthenticatedRequestToUserService() {
        webTestClient.get()
                .uri("/gateway/users/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(123)
                .jsonPath("$.source").isEqualTo("user-service");
    }

    @Test
    void shouldReturnRequestIdHeader() {

        webTestClient.get()
                .uri("/gateway/unknown")
                .exchange()
                .expectHeader()
                .exists("X-Request-Id");
    }

    @Test
    void shouldPreserveProvidedRequestId() {

        String requestId = "test-request-123";

        webTestClient.get()
                .uri("/gateway/unknown")
                .header("X-Request-Id", requestId)
                .exchange()
                .expectHeader()
                .valueEquals("X-Request-Id", requestId);
    }
}