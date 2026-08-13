package com.joven.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating and updating categories.
 * Contains validated fields for category name and description.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    /**
     * The category name. Must not be blank and cannot exceed 100 characters.
     */
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    /**
     * The category description. Optional, cannot exceed 255 characters.
     */
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
