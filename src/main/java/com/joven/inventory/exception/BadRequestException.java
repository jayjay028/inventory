package com.joven.inventory.exception;

/**
 * Exception thrown when a client request is invalid or malformed.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Constructs a new BadRequestException with the specified message.
     *
     * @param message the detail message
     */
    public BadRequestException(String message) {
        super(message);
    }
}
