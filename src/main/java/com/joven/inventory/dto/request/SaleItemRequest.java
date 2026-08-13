package com.joven.inventory.dto.request;

import com.joven.inventory.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for individual sale line items.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemRequest {

    @NotNull
    private Long itemId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal unitPrice;

    @Builder.Default
    private DiscountType discountType = DiscountType.NONE;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Builder.Default
    private BigDecimal discountValue = BigDecimal.ZERO;
}
