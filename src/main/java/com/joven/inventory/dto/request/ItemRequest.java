package com.joven.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating an inventory item.
 * Contains validation constraints for all required fields.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 200, message = "Item name must not exceed 200 characters")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Unit is required")
    @Size(max = 30, message = "Unit must not exceed 30 characters")
    private String unit;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0", message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0", message = "Cost price must be greater than or equal to 0")
    private BigDecimal costPrice;

    @NotNull(message = "Reorder level is required")
    @Min(value = 0, message = "Reorder level must be greater than or equal to 0")
    private Integer reorderLevel;

    @NotNull(message = "Taxable flag is required")
    private Boolean taxable;
}
