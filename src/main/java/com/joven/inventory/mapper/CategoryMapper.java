package com.joven.inventory.mapper;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CategoryRequest;
import com.joven.inventory.dto.response.CategoryResponse;
import com.joven.inventory.entity.Category;
import org.springframework.data.domain.Page;

/**
 * Utility mapper class for converting between {@link Category} entities
 * and their corresponding DTOs.
 *
 * <p>This class uses static methods and cannot be instantiated.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class CategoryMapper {

    /**
     * Private constructor to prevent instantiation.
     */
    private CategoryMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a {@link Category} entity to a {@link CategoryResponse} DTO.
     *
     * @param category the category entity to convert
     * @return the corresponding category response DTO
     */
    public static CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createdBy(category.getCreatedBy())
                .createdAt(category.getCreatedAt())
                .updatedBy(category.getUpdatedBy())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * Converts a {@link Page} of {@link Category} entities to a {@link PageResponse}
     * of {@link CategoryResponse} DTOs.
     *
     * @param page the page of category entities
     * @return the page response containing category response DTOs
     */
    public static PageResponse<CategoryResponse> toPageResponse(Page<Category> page) {
        Page<CategoryResponse> responsePage = page.map(CategoryMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    /**
     * Updates an existing {@link Category} entity with values from a {@link CategoryRequest} DTO.
     *
     * @param category the category entity to update
     * @param request  the request DTO containing new values
     */
    public static void updateEntity(Category category, CategoryRequest request) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
    }
}
