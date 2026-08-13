package com.joven.inventory.service;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CategoryRequest;
import com.joven.inventory.dto.response.CategoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for category management operations.
 * Provides methods for CRUD operations and status management.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface CategoryService {

    /**
     * Retrieves all categories with pagination support.
     *
     * @param pageable the pagination parameters
     * @return a paginated response of category DTOs
     */
    PageResponse<CategoryResponse> getAll(Pageable pageable);

    /**
     * Retrieves all active categories ordered by name.
     *
     * @return a list of active category DTOs
     */
    List<CategoryResponse> getAllActive();

    /**
     * Retrieves a single category by its unique identifier.
     *
     * @param id the category ID
     * @return the category response DTO
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the category is not found
     */
    CategoryResponse getById(Long id);

    /**
     * Creates a new category.
     *
     * @param request the category request DTO containing creation data
     * @return the created category response DTO
     * @throws com.joven.inventory.exception.DuplicateResourceException if a category with the same name exists
     */
    CategoryResponse create(CategoryRequest request);

    /**
     * Updates an existing category.
     *
     * @param id      the ID of the category to update
     * @param request the category request DTO containing updated data
     * @return the updated category response DTO
     * @throws com.joven.inventory.exception.ResourceNotFoundException  if the category is not found
     * @throws com.joven.inventory.exception.DuplicateResourceException if another category with the same name exists
     */
    CategoryResponse update(Long id, CategoryRequest request);

    /**
     * Updates the active status of a category.
     *
     * @param id     the ID of the category to update
     * @param active the new active status
     * @return the updated category response DTO
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the category is not found
     */
    CategoryResponse updateStatus(Long id, boolean active);
}
