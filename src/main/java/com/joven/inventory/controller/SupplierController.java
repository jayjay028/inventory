package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.SupplierRequest;
import com.joven.inventory.dto.response.SupplierResponse;
import com.joven.inventory.service.SupplierService;
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
 * REST controller for supplier management operations.
 * Provides endpoints for CRUD operations and search functionality.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * Retrieves all active suppliers with pagination.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return the API response containing a page of suppliers
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SupplierResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<SupplierResponse> response = supplierService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Suppliers retrieved successfully", response));
    }

    /**
     * Retrieves a specific supplier by ID.
     *
     * @param id the supplier ID
     * @return the API response containing the supplier data
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> getById(@PathVariable Long id) {
        SupplierResponse response = supplierService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier retrieved successfully", response));
    }

    /**
     * Searches suppliers by name or TIN.
     *
     * @param q        the search query
     * @param pageable pagination parameters
     * @return the API response containing a page of matching suppliers
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<SupplierResponse>>> search(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<SupplierResponse> response = supplierService.search(q, pageable);
        return ResponseEntity.ok(ApiResponse.success("Supplier search completed", response));
    }

    /**
     * Creates a new supplier.
     *
     * @param request the supplier request data
     * @return the API response containing the created supplier
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponse>> create(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier created successfully", response));
    }

    /**
     * Updates an existing supplier.
     *
     * @param id      the supplier ID
     * @param request the supplier request data with updated values
     * @return the API response containing the updated supplier
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", response));
    }

    /**
     * Updates the active status of a supplier.
     *
     * @param id     the supplier ID
     * @param active the new active status
     * @return the API response containing the updated supplier
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        SupplierResponse response = supplierService.updateStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Supplier status updated successfully", response));
    }
}
