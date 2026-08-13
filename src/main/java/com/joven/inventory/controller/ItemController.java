package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.ItemRequest;
import com.joven.inventory.dto.response.ItemResponse;
import com.joven.inventory.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
 * REST controller for inventory item management.
 * Provides endpoints for CRUD operations, searching, filtering by category,
 * and querying low-stock items.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * Retrieves all active items with optional category filter and search.
     * Supports pagination via query parameters (page, size, sort).
     *
     * @param categoryId optional category ID to filter items
     * @param search optional search query to filter by name or item code
     * @param pageable pagination information
     * @return the API response containing a page of items
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItemResponse>>> getAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        PageResponse<ItemResponse> response;

        if (search != null && !search.isBlank()) {
            response = itemService.search(search, pageable);
        } else if (categoryId != null) {
            response = itemService.getAll(categoryId, pageable);
        } else {
            response = itemService.getAll(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success("Items retrieved successfully", response));
    }

    /**
     * Retrieves a single item by its ID.
     *
     * @param id the item ID
     * @return the API response containing the item details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getById(@PathVariable Long id) {
        ItemResponse response = itemService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Item retrieved successfully", response));
    }

    /**
     * Creates a new inventory item.
     *
     * @param request the item creation request
     * @return the API response containing the created item
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> create(@Valid @RequestBody ItemRequest request) {
        ItemResponse response = itemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item created successfully", response));
    }

    /**
     * Updates an existing inventory item.
     *
     * @param id the item ID to update
     * @param request the item update request
     * @return the API response containing the updated item
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request) {
        ItemResponse response = itemService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully", response));
    }

    /**
     * Updates the active status of an item.
     *
     * @param id the item ID
     * @param active the new active status
     * @return the API response containing the updated item
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ItemResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        ItemResponse response = itemService.updateStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Item status updated successfully", response));
    }

    /**
     * Searches items by name or item code.
     *
     * @param q the search query
     * @param pageable pagination information
     * @return the API response containing matching items
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ItemResponse>>> search(
            @RequestParam String q,
            Pageable pageable) {
        PageResponse<ItemResponse> response = itemService.search(q, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", response));
    }

    /**
     * Retrieves items with stock levels at or below their reorder level.
     *
     * @param pageable pagination information
     * @return the API response containing low-stock items
     */
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<PageResponse<ItemResponse>>> getLowStock(Pageable pageable) {
        PageResponse<ItemResponse> response = itemService.getLowStock(pageable);
        return ResponseEntity.ok(ApiResponse.success("Low stock items retrieved successfully", response));
    }
}
