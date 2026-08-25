package com.joven.inventory.service;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.StoreRequest;
import com.joven.inventory.dto.response.StoreResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for store management operations.
 * Provides methods for CRUD operations and status management.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface StoreService {

    /**
     * Retrieves all stores with pagination support.
     *
     * @param pageable the pagination parameters
     * @return a paginated response of store DTOs
     */
    PageResponse<StoreResponse> getAll(Pageable pageable);

    /**
     * Retrieves all active stores.
     *
     * @return a list of active store DTOs
     */
    List<StoreResponse> getAllActive();

    /**
     * Retrieves a single store by its unique identifier.
     *
     * @param id the store ID
     * @return the store response DTO
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the store is not found
     */
    StoreResponse getById(Long id);

    /**
     * Creates a new store.
     *
     * @param request the store request DTO containing creation data
     * @return the created store response DTO
     * @throws com.joven.inventory.exception.DuplicateResourceException if a store with the same code exists
     */
    StoreResponse create(StoreRequest request);

    /**
     * Updates an existing store.
     *
     * @param id      the ID of the store to update
     * @param request the store request DTO containing updated data
     * @return the updated store response DTO
     * @throws com.joven.inventory.exception.ResourceNotFoundException  if the store is not found
     * @throws com.joven.inventory.exception.DuplicateResourceException if another store with the same code exists
     */
    StoreResponse update(Long id, StoreRequest request);

    /**
     * Updates the active status of a store.
     *
     * @param id     the ID of the store to update
     * @param active the new active status
     * @return the updated store response DTO
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the store is not found
     */
    StoreResponse updateStatus(Long id, boolean active);
}
