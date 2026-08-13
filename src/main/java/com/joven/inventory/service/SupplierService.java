package com.joven.inventory.service;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.SupplierRequest;
import com.joven.inventory.dto.response.SupplierResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for supplier operations.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface SupplierService {

    /**
     * Retrieves all active suppliers with pagination.
     *
     * @param pageable pagination information
     * @return a page response of supplier DTOs
     */
    PageResponse<SupplierResponse> getAll(Pageable pageable);

    /**
     * Retrieves a supplier by its ID.
     *
     * @param id the supplier ID
     * @return the supplier response DTO
     */
    SupplierResponse getById(Long id);

    /**
     * Searches suppliers by name or TIN.
     *
     * @param query    the search query
     * @param pageable pagination information
     * @return a page response of matching supplier DTOs
     */
    PageResponse<SupplierResponse> search(String query, Pageable pageable);

    /**
     * Creates a new supplier.
     *
     * @param request the supplier request DTO
     * @return the created supplier response DTO
     */
    SupplierResponse create(SupplierRequest request);

    /**
     * Updates an existing supplier.
     *
     * @param id      the supplier ID
     * @param request the supplier request DTO with updated values
     * @return the updated supplier response DTO
     */
    SupplierResponse update(Long id, SupplierRequest request);

    /**
     * Updates the active status of a supplier.
     *
     * @param id     the supplier ID
     * @param active the new active status
     * @return the updated supplier response DTO
     */
    SupplierResponse updateStatus(Long id, boolean active);
}
