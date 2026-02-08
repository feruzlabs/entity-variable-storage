package com.evs.exception;

/**
 * Base exception for Entity Variable Storage operations.
 */
public class EVSException extends RuntimeException {

    public EVSException(String message) {
        super(message);
    }

    public EVSException(String message, Throwable cause) {
        super(message, cause);
    }

    public EVSException(Throwable cause) {
        super(cause);
    }
}
