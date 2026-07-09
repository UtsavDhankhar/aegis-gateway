package com.aegis.gateway.proxy;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ProxyIntegrationText {

    private static HttpServer userService;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void startBackend() throws IOException {

        userService = HttpServer.create(new InetSocketAddress(9001), 0);

        userService.createContext("/gateway/users/123", exchange -> {
            byte[] response = """
                    {
                      "id": 123,
                      "name": "Amoeba",
                      "source": "user-service"
                    }
                    """.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders()
                    .add("Content-Type", "application/json");

            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });

        userService.start();
    }

    @AfterAll
    static void stopBackend() {
        if (userService != null) {
            userService.stop(0);
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
}

