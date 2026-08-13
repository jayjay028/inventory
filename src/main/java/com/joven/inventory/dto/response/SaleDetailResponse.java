package com.joven.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sale detail response DTO including line items, addons, and payments.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetailResponse {

    // Fields from SaleResponse (summary)
    private Long id;
    private String saleNo;
    private Long customerId;
    private String customerName;
    private Long shiftId;
    private BigDecimal subtotal;
    private String discountType;
    private BigDecimal discountAmount;
    private BigDecimal addonsTotal;
    private BigDecimal netAmount;
    private Boolean taxEnabled;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountTendered;
    private BigDecimal changeAmount;
    private String paymentMethod;
    private String status;
    private String documentType;
    private String documentNo;
    private String remarks;
    private LocalDateTime saleDate;
    private String createdBy;
    private LocalDateTime createdAt;
    private Integer itemCount;

    // Additional detail fields
    private BigDecimal discountValue;
    private String taxType;
    private BigDecimal taxRate;
    private BigDecimal vatableAmount;
    private String voidReason;
    private String voidedBy;
    private LocalDateTime voidedAt;
    private List<SaleItemResponse> items;
    private List<SaleAddonResponse> addons;
    private List<SalePaymentResponse> payments;

    /**
     * Sale line item response.
     *
     * @author Joven Q. Divinagracia Jr.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemResponse {

        private Long id;
        private Long itemId;
        private String itemCode;
        private String itemName;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal unitCost;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal discountAmount;
        private BigDecimal lineTotal;
    }

    /**
     * Sale addon response.
     *
     * @author Joven Q. Divinagracia Jr.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleAddonResponse {

        private Long id;
        private String addonName;
        private BigDecimal amount;
    }

    /**
     * Sale payment response.
     *
     * @author Joven Q. Divinagracia Jr.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalePaymentResponse {

        private Long id;
        private String paymentMethod;
        private BigDecimal amount;
        private String referenceNo;
    }
}
