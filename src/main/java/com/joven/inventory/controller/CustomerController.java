package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CustomerRequest;
import com.joven.inventory.dto.response.CustomerResponse;
import com.joven.inventory.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for customer management operations.
 * Provides endpoints for CRUD operations and search functionality.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Retrieves all active customers with pagination.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return the API response containing a page of customers
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<CustomerResponse> response = customerService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", response));
    }

    /**
     * Retrieves a specific customer by ID.
     *
     * @param id the customer ID
     * @return the API response containing the customer data
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        CustomerResponse response = customerService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", response));
    }

    /**
     * Searches customers by name or TIN.
     *
     * @param q        the search query
     * @param pageable pagination parameters
     * @return the API response containing a page of matching customers
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> search(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<CustomerResponse> response = customerService.search(q, pageable);
        return ResponseEntity.ok(ApiResponse.success("Customer search completed", response));
    }

    /**
     * Creates a new customer.
     *
     * @param request the customer request data
     * @return the API response containing the created customer
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", response));
    }

    /**
     * Updates an existing customer.
     *
     * @param id      the customer ID
     * @param request the customer request data with updated values
     * @return the API response containing the updated customer
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", response));
    }

    /**
     * Updates the active status of a customer.
     *
     * @param id     the customer ID
     * @param active the new active status
     * @return the API response containing the updated customer
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        CustomerResponse response = customerService.updateStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Customer status updated successfully", response));
    }
}
