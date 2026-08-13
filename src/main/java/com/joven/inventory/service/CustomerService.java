package com.joven.inventory.service;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CustomerRequest;
import com.joven.inventory.dto.response.CustomerResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for customer operations.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface CustomerService {

    /**
     * Retrieves all active customers with pagination.
     *
     * @param pageable pagination information
     * @return a page response of customer DTOs
     */
    PageResponse<CustomerResponse> getAll(Pageable pageable);

    /**
     * Retrieves a customer by its ID.
     *
     * @param id the customer ID
     * @return the customer response DTO
     */
    CustomerResponse getById(Long id);

    /**
     * Searches customers by name or TIN.
     *
     * @param query    the search query
     * @param pageable pagination information
     * @return a page response of matching customer DTOs
     */
    PageResponse<CustomerResponse> search(String query, Pageable pageable);

    /**
     * Creates a new customer.
     *
     * @param request the customer request DTO
     * @return the created customer response DTO
     */
    CustomerResponse create(CustomerRequest request);

    /**
     * Updates an existing customer.
     *
     * @param id      the customer ID
     * @param request the customer request DTO with updated values
     * @return the updated customer response DTO
     */
    CustomerResponse update(Long id, CustomerRequest request);

    /**
     * Updates the active status of a customer.
     *
     * @param id     the customer ID
     * @param active the new active status
     * @return the updated customer response DTO
     */
    CustomerResponse updateStatus(Long id, boolean active);
}
