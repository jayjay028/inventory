package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CategoryRequest;
import com.joven.inventory.dto.response.CategoryResponse;
import com.joven.inventory.service.CategoryService;
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
 * REST controller for category management operations.
 * Provides endpoints for CRUD operations and status management of categories.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Retrieves all categories with pagination support.
     * Optionally filters by search term matching category name.
     *
     * @param page   the page number (zero-based), defaults to 0
     * @param size   the page size, defaults to 10
     * @param sortBy the field to sort by, defaults to "name"
     * @param sortDir the sort direction (asc or desc), defaults to "asc"
     * @param search optional search term to filter categories by name
     * @return the API response containing a paginated list of categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<CategoryResponse> response = categoryService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", response));
    }

    /**
     * Retrieves all active categories as a simple list (no pagination).
     * Useful for dropdown or selection components.
     *
     * @return the API response containing a list of active categories
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllActive() {
        List<CategoryResponse> response = categoryService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success("Active categories retrieved successfully", response));
    }

    /**
     * Retrieves a single category by its unique identifier.
     *
     * @param id the category ID
     * @return the API response containing the category details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        CategoryResponse response = categoryService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", response));
    }

    /**
     * Creates a new category.
     *
     * @param request the category request body containing name and description
     * @return the API response containing the created category
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    /**
     * Updates an existing category.
     *
     * @param id      the ID of the category to update
     * @param request the category request body containing updated name and description
     * @return the API response containing the updated category
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    /**
     * Updates the active status of a category.
     *
     * @param id     the ID of the category to update
     * @param active the new active status
     * @return the API response containing the updated category
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        CategoryResponse response = categoryService.updateStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Category status updated successfully", response));
    }
}
