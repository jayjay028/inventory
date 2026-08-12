package com.joven.inventory.exception;

/**
 * Exception thrown when an operation requires more stock than is available.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public class InsufficientStockException extends RuntimeException {

    private final Long itemId;
    private final int available;
    private final int requested;

    /**
     * Constructs a new InsufficientStockException.
     *
     * @param itemId    the ID of the item with insufficient stock
     * @param available the available stock quantity
     * @param requested the requested stock quantity
     */
    public InsufficientStockException(Long itemId, int available, int requested) {
        super(String.format("Insufficient stock for item ID %d: available=%d, requested=%d",
                itemId, available, requested));
        this.itemId = itemId;
        this.available = available;
        this.requested = requested;
    }

    /**
     * Returns the item ID with insufficient stock.
     *
     * @return the item ID
     */
    public Long getItemId() {
        return itemId;
    }

    /**
     * Returns the available stock quantity.
     *
     * @return the available quantity
     */
    public int getAvailable() {
        return available;
    }

    /**
     * Returns the requested stock quantity.
     *
     * @return the requested quantity
     */
    public int getRequested() {
        return requested;
    }

    @Override
    public String getMessage() {
        return String.format("Insufficient stock for item ID %d: available=%d, requested=%d",
                itemId, available, requested);
    }
}
