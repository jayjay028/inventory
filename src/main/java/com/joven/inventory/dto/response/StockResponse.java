package com.joven.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for stock/inventory item summary.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {

    private Long id;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private String categoryName;
    private String unit;
    private Integer quantityOnHand;
    private Integer reorderLevel;
    private BigDecimal price;
    private BigDecimal costPrice;
    private LocalDateTime lastUpdated;
    private String stockStatus;
}
