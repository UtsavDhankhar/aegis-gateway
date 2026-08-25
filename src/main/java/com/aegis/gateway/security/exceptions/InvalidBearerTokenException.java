package com.aegis.gateway.security.exceptions;

public class InvalidBearerTokenException extends RuntimeException {

    public InvalidBearerTokenException(String message) {
        super(message);
    }
}
