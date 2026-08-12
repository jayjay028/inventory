package com.joven.inventory.exception;

/**
 * Exception thrown when an authenticated user does not have permission to access a resource.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Constructs a new ForbiddenException with the specified message.
     *
     * @param message the detail message
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
