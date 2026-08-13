package com.joven.inventory.service;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.ItemRequest;
import com.joven.inventory.dto.response.ItemResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for inventory item management operations.
 * Provides methods for CRUD operations, search, and stock-level queries.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface ItemService {

    /**
     * Retrieves all active items with pagination.
     *
     * @param pageable pagination information
     * @return a page response containing item DTOs
     */
    PageResponse<ItemResponse> getAll(Pageable pageable);

    /**
     * Retrieves active items filtered by category with pagination.
     *
     * @param categoryId the category ID to filter by
     * @param pageable pagination information
     * @return a page response containing item DTOs in the specified category
     */
    PageResponse<ItemResponse> getAll(Long categoryId, Pageable pageable);

    /**
     * Retrieves a single item by its ID.
     *
     * @param id the item ID
     * @return the item response DTO
     */
    ItemResponse getById(Long id);

    /**
     * Searches items by name or item code.
     *
     * @param query the search query string
     * @param pageable pagination information
     * @return a page response containing matching item DTOs
     */
    PageResponse<ItemResponse> search(String query, Pageable pageable);

    /**
     * Retrieves items whose current stock is at or below the reorder level.
     *
     * @param pageable pagination information
     * @return a page response containing low-stock item DTOs
     */
    PageResponse<ItemResponse> getLowStock(Pageable pageable);

    /**
     * Creates a new item with auto-generated item code and initializes stock record.
     *
     * @param request the item creation request
     * @return the created item response DTO
     */
    ItemResponse create(ItemRequest request);

    /**
     * Updates an existing item.
     *
     * @param id the item ID to update
     * @param request the item update request
     * @return the updated item response DTO
     */
    ItemResponse update(Long id, ItemRequest request);

    /**
     * Updates the active status of an item.
     *
     * @param id the item ID
     * @param active the new active status
     * @return the updated item response DTO
     */
    ItemResponse updateStatus(Long id, boolean active);
}
