package com.aegis.gateway.context;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public record GatewayResponse(
        HttpStatus status,
        HttpHeaders headers,
        String body
) {

    public static GatewayResponse ok(String body) {
        return new GatewayResponse(HttpStatus.OK, new HttpHeaders(), body);
    }

    public static GatewayResponse error(HttpStatus httpStatus, String body) {
        return new GatewayResponse(httpStatus, new HttpHeaders(), body);
    }
}
