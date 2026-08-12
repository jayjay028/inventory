package com.joven.inventory.exception;

/**
 * Exception thrown when a business rule violation occurs.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public class BusinessRuleException extends RuntimeException {

    /**
     * Constructs a new BusinessRuleException with the specified message.
     *
     * @param message the detail message describing the violated business rule
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
