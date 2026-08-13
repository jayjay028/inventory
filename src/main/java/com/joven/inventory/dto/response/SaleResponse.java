package com.joven.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sale response DTO for list views.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {

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
}
