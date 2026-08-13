package com.joven.inventory.dto.request;

import com.joven.inventory.enums.DocumentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for stock-in transactions.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockInRequest {

    @NotNull
    private Long itemId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal unitCost;

    private Long supplierId;

    private DocumentType documentType;

    @Size(max = 50)
    private String referenceNo;

    @Builder.Default
    private Boolean taxEnabled = false;

    @NotNull
    private LocalDateTime transactionDate;

    @Size(max = 500)
    private String remarks;

    @Valid
    private List<AddonRequest> addons;

    /**
     * Inner DTO for addon line items.
     *
     * @author Joven Q. Divinagracia Jr.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddonRequest {

        @NotBlank
        @Size(max = 100)
        private String addonName;

        @NotNull
        @DecimalMin("0.00")
        private BigDecimal amount;
    }
}
