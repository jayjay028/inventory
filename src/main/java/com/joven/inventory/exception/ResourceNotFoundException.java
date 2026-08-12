package com.joven.inventory.exception;

/**
 * Exception thrown when a requested resource is not found.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with a formatted message.
     *
     * @param resourceName the name of the resource (e.g., "Category")
     * @param fieldName    the field used for lookup (e.g., "id")
     * @param fieldValue   the value of the field (e.g., 5)
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}
