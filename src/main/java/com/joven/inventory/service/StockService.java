package com.joven.inventory.service;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.response.StockResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for stock/inventory level operations.
 * Provides methods to query current stock levels and modify quantities
 * when transactions are approved.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface StockService {

    /**
     * Retrieves all stock records with pagination, including item details.
     *
     * @param pageable pagination information
     * @return a paginated response of stock records
     */
    PageResponse<StockResponse> getAll(Pageable pageable);

    /**
     * Retrieves the stock record for a specific item.
     *
     * @param itemId the ID of the item
     * @return the stock response for the item
     * @throws com.joven.inventory.exception.ResourceNotFoundException if no stock record exists for the item
     */
    StockResponse getByItemId(Long itemId);

    /**
     * Adds quantity to the item's current stock level.
     * Used when a stock-in transaction is approved.
     *
     * @param itemId   the ID of the item
     * @param quantity the quantity to add (must be positive)
     * @throws com.joven.inventory.exception.ResourceNotFoundException if no stock record exists for the item
     */
    void addStock(Long itemId, int quantity);

    /**
     * Deducts quantity from the item's current stock level.
     * Validates that sufficient stock is available before deducting.
     * Used when a stock-out transaction is approved.
     *
     * @param itemId   the ID of the item
     * @param quantity the quantity to deduct (must be positive)
     * @throws com.joven.inventory.exception.ResourceNotFoundException   if no stock record exists for the item
     * @throws com.joven.inventory.exception.InsufficientStockException if available stock is less than the requested quantity
     */
    void deductStock(Long itemId, int quantity);

    /**
     * Sets the item's stock level to an exact quantity.
     * Used when a stock adjustment transaction is approved.
     *
     * @param itemId   the ID of the item
     * @param quantity the new quantity on hand
     * @throws com.joven.inventory.exception.ResourceNotFoundException if no stock record exists for the item
     */
    void setStock(Long itemId, int quantity);
}
