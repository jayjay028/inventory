package com.joven.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for stock adjustment transactions.
 * The quantity is always positive; the adjustment type determines direction.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustRequest {

    @NotNull
    private Long itemId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private LocalDateTime transactionDate;

    @Size(max = 500)
    private String remarks;
}
