package com.joven.inventory.exception;

/**
 * Exception thrown when a request lacks valid authentication credentials.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedException with the specified message.
     *
     * @param message the detail message
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
