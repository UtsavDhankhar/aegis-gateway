package com.aegis.gateway.routing.exceptions;


public final class InvalidRouteConfigurationException extends RuntimeException {

    public InvalidRouteConfigurationException(String message) {
        super(message);
    }

    public InvalidRouteConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
