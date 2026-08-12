package com.joven.inventory.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructs a new DuplicateResourceException with the specified message.
     *
     * @param message the detail message
     */
    public DuplicateResourceException(String message) {
        super(message);
    }

    /**
     * Constructs a new DuplicateResourceException with a formatted message.
     *
     * @param resourceName the name of the resource (e.g., "Item")
     * @param fieldName    the field that caused the conflict (e.g., "item_code")
     * @param fieldValue   the value of the field (e.g., "ITM-001")
     */
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue));
    }
}
