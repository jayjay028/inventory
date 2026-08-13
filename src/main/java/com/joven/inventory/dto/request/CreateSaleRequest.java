package com.joven.inventory.dto.request;

import com.joven.inventory.enums.DiscountType;
import com.joven.inventory.enums.DocumentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating a new sale transaction.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSaleRequest {

    private Long customerId;

    @NotEmpty
    @Valid
    private List<SaleItemRequest> items;

    @Valid
    private List<SaleAddonRequest> addons;

    @Builder.Default
    private DiscountType discountType = DiscountType.NONE;

    @DecimalMin("0.00")
    @Builder.Default
    private BigDecimal discountValue = BigDecimal.ZERO;

    @Builder.Default
    private Boolean taxEnabled = false;

    @Builder.Default
    private DocumentType documentType = DocumentType.OR;

    @Size(max = 500)
    private String remarks;

    /**
     * Inner DTO for sale addon line items.
     *
     * @author Joven Q. Divinagracia Jr.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleAddonRequest {

        @NotBlank
        @Size(max = 100)
        private String addonName;

        @NotNull
        @DecimalMin("0.01")
        private BigDecimal amount;
    }
}
