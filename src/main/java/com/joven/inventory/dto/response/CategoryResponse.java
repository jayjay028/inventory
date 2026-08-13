package com.joven.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for category data returned to API consumers.
 * Contains all category fields including audit information.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    /**
     * The unique identifier of the category.
     */
    private Long id;

    /**
     * The category name.
     */
    private String name;

    /**
     * The category description.
     */
    private String description;

    /**
     * Whether the category is active.
     */
    private Boolean active;

    /**
     * The username of the user who created this category.
     */
    private String createdBy;

    /**
     * The timestamp when this category was created.
     */
    private LocalDateTime createdAt;

    /**
     * The username of the user who last updated this category.
     */
    private String updatedBy;

    /**
     * The timestamp when this category was last updated.
     */
    private LocalDateTime updatedAt;
}
