package com.joven.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shift summary response DTO with sales breakdown by payment method.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftSummaryResponse {

    private Long shiftId;
    private String cashier;
    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private BigDecimal openingAmount;
    private BigDecimal closingAmount;
    private BigDecimal expectedAmount;
    private BigDecimal difference;
    private BigDecimal totalSales;
    private Integer totalTransactions;
    private BigDecimal totalVoided;
    private BigDecimal cashSales;
    private BigDecimal gcashSales;
    private BigDecimal bankTransferSales;
    private BigDecimal creditSales;
}
