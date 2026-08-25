package com.aegis.gateway.observability;

import com.aegis.gateway.context.GatewayResponse;

public final class GatewayResponseStatus {

    private GatewayResponseStatus() {
    }

    public static int statusCode(GatewayResponse response) {
        return switch (response) {

            case GatewayResponse.SimpleGatewayResponse simpleResponse -> simpleResponse.status().value();

            case GatewayResponse.NativeGatewayResponse nativeResponse ->
                    nativeResponse.serverResponse().statusCode().value();
        };
    }
}