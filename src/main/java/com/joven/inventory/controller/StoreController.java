package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.StoreRequest;
import com.joven.inventory.dto.response.StoreResponse;
import com.joven.inventory.security.Permission;
import com.joven.inventory.security.RequiresPermission;
import com.joven.inventory.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.util.List;

/**
 * REST controller for store management operations.
 * Provides endpoints for CRUD operations and status management of stores.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * Retrieves all stores with pagination support.
     *
     * @param page    the page number (zero-based), defaults to 0
     * @param size    the page size, defaults to 10
     * @param sortBy  the field to sort by, defaults to "name"
     * @param sortDir the sort direction (asc or desc), defaults to "asc"
     * @return the API response containing a paginated list of stores
     */
    @GetMapping
    @RequiresPermission(Permission.MANAGE_SETTINGS)
    public ResponseEntity<ApiResponse<PageResponse<StoreResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<StoreResponse> response = storeService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Stores retrieved successfully", response));
    }

    /**
     * Retrieves all active stores as a simple list (no pagination).
     * Available to any authenticated user so they can select their store.
     *
     * @return the API response containing a list of active stores
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getAllActive() {
        List<StoreResponse> response = storeService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success("Active stores retrieved successfully", response));
    }

    /**
     * Retrieves a single store by its unique identifier.
     *
     * @param id the store ID
     * @return the API response containing the store details
     */
    @GetMapping("/{id}")
    @RequiresPermission(Permission.MANAGE_SETTINGS)
    public ResponseEntity<ApiResponse<StoreResponse>> getById(@PathVariable Long id) {
        StoreResponse response = storeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Store retrieved successfully", response));
    }

    /**
     * Creates a new store.
     *
     * @param request the store request body containing code, name, and contact details
     * @return the API response containing the created store
     */
    @PostMapping
    @RequiresPermission(Permission.MANAGE_SETTINGS)
    public ResponseEntity<ApiResponse<StoreResponse>> create(@Valid @RequestBody StoreRequest request) {
        StoreResponse response = storeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Store created successfully", response));
    }

    /**
     * Updates an existing store.
     *
     * @param id      the ID of the store to update
     * @param request the store request body containing updated data
     * @return the API response containing the updated store
     */
    @PutMapping("/{id}")
    @RequiresPermission(Permission.MANAGE_SETTINGS)
    public ResponseEntity<ApiResponse<StoreResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody StoreRequest request) {
        StoreResponse response = storeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Store updated successfully", response));
    }

    /**
     * Updates the active status of a store.
     *
     * @param id     the ID of the store to update
     * @param active the new active status
     * @return the API response containing the updated store
     */
    @PatchMapping("/{id}/status")
    @RequiresPermission(Permission.MANAGE_SETTINGS)
    public ResponseEntity<ApiResponse<StoreResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        StoreResponse response = storeService.updateStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Store status updated successfully", response));
    }
}
