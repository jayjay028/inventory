package com.joven.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing an inventory item with its associated category details.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private String itemCode;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private String unit;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer reorderLevel;
    private Boolean taxable;
    private Boolean active;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
