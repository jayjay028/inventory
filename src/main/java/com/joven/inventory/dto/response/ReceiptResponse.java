package com.joven.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Receipt response DTO formatted for receipt printing.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {

    private String businessName;
    private String businessTin;
    private String businessAddress;
    private String businessContact;
    private Boolean vatRegistered;
    private String saleNo;
    private String documentType;
    private String documentNo;
    private LocalDateTime saleDate;
    private String cashierName;
    private String customerName;
    private String customerTin;
    private List<ReceiptItem> items;
    private BigDecimal subtotal;
    private String discountLabel;
    private BigDecimal discountAmount;
    private BigDecimal addonsTotal;
    private BigDecimal vatableSales;
    private BigDecimal vatAmount;
    private BigDecimal vatExemptSales;
    private BigDecimal zeroRatedSales;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private BigDecimal amountTendered;
    private BigDecimal changeAmount;
    private List<ReceiptPayment> payments;
    private String receiptFooter;

    /**
     * Receipt line item.
     *
     * @author Joven Q. Divinagracia Jr.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptItem {

        private String itemName;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
    }

    /**
     * Receipt payment entry for split payments.
     *
     * @author Joven Q. Divinagracia Jr.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptPayment {

        private String method;
        private BigDecimal amount;
        private String referenceNo;
    }
}
