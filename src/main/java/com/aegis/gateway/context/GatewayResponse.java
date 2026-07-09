package com.aegis.gateway.context;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.server.ServerResponse;

public sealed interface GatewayResponse {

    public static GatewayResponse ok(String body) {
        return new SimpleGatewayResponse(HttpStatus.OK, new HttpHeaders(), body);
    }

    public static GatewayResponse error(HttpStatus httpStatus, String body) {
        return new SimpleGatewayResponse(httpStatus, new HttpHeaders(), body);
    }

    public static GatewayResponse json(HttpStatusCode statusCode, Object body) {
        return new SimpleGatewayResponse(statusCode, new HttpHeaders(), body);
    }

    public static GatewayResponse nativeResponse(ServerResponse serverResponse) {
        return new NativeGatewayResponse(serverResponse);
    }


    record SimpleGatewayResponse(
                                 HttpStatusCode status,
                                 HttpHeaders headers,
                                 Object body
    ) implements GatewayResponse{}

    record NativeGatewayResponse (ServerResponse serverResponse)
            implements GatewayResponse{}
}
