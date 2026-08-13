package com.joven.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for stock transaction details.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionResponse {

    private Long id;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private String transactionType;
    private String status;
    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal unitPrice;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal subtotal;
    private BigDecimal netAmount;
    private Boolean taxEnabled;
    private String taxType;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal vatableAmount;
    private BigDecimal totalAmount;
    private Long customerId;
    private String customerName;
    private Long supplierId;
    private String supplierName;
    private String documentType;
    private String documentNo;
    private String referenceNo;
    private String remarks;
    private LocalDateTime transactionDate;
    private String createdBy;
    private LocalDateTime createdAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private List<AddonResponse> addons;

    /**
     * Inner response DTO for addon line items.
     *
     * @author Joven Q. Divinagracia Jr.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddonResponse {

        private Long id;
        private String addonName;
        private BigDecimal amount;
    }
}
