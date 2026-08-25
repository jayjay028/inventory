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
     * Retrieves the stock record for a specific item within a store.
     *
     * @param itemId  the ID of the item
     * @param storeId the ID of the store
     * @return the stock response for the item in the given store
     * @throws com.joven.inventory.exception.ResourceNotFoundException if no stock record exists for the item in the store
     */
    StockResponse getByItemIdAndStore(Long itemId, Long storeId);

    /**
     * Adds quantity to the item's current stock level for a store.
     * Used when a stock-in transaction is approved. If no stock record
     * exists for the (item, store) pair, a new one is created.
     *
     * @param itemId   the ID of the item
     * @param storeId  the ID of the store
     * @param quantity the quantity to add (must be positive)
     */
    void addStock(Long itemId, Long storeId, int quantity);

    /**
     * Deducts quantity from the item's current stock level for a store.
     * Validates that sufficient stock is available before deducting.
     * Used when a stock-out transaction is approved.
     *
     * @param itemId   the ID of the item
     * @param storeId  the ID of the store
     * @param quantity the quantity to deduct (must be positive)
     * @throws com.joven.inventory.exception.ResourceNotFoundException   if no stock record exists for the item in the store
     * @throws com.joven.inventory.exception.InsufficientStockException if available stock is less than the requested quantity
     */
    void deductStock(Long itemId, Long storeId, int quantity);

    /**
     * Sets the item's stock level to an exact quantity for a store.
     * Used when a stock adjustment transaction is approved. If no stock record
     * exists for the (item, store) pair, a new one is created.
     *
     * @param itemId   the ID of the item
     * @param storeId  the ID of the store
     * @param quantity the new quantity on hand
     */
    void setStock(Long itemId, Long storeId, int quantity);
}
