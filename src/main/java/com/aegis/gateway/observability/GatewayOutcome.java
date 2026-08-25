package com.aegis.gateway.observability;

public enum GatewayOutcome {

    SUCCESS,
    REDIRECTION,
    CLIENT_ERROR,
    SERVER_ERROR,
    OTHER;

    public static GatewayOutcome fromStatus(int status) {

        if (status >= 200 && status < 300) {
            return SUCCESS;
        }

        if (status >= 300 && status < 400) {
            return REDIRECTION;
        }

        if (status >= 400 && status < 500) {
            return CLIENT_ERROR;
        }

        if (status >= 500 && status < 600) {
            return SERVER_ERROR;
        }

        return OTHER;
    }
}
