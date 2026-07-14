package com.aegis.gateway.routing.exceptions;

public final class InvalidRouteMetadataException extends RuntimeException {

    public InvalidRouteMetadataException(String message) {
        super(message);
    }

    public InvalidRouteMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}