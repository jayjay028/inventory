package com.joven.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shift response DTO.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftResponse {

    private Long id;
    private String cashier;
    private BigDecimal openingAmount;
    private BigDecimal closingAmount;
    private BigDecimal expectedAmount;
    private BigDecimal difference;
    private BigDecimal totalSales;
    private Integer totalTransactions;
    private BigDecimal totalVoided;
    private BigDecimal totalReturns;
    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private String remarks;
}
