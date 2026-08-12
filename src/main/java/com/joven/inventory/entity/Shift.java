package com.joven.inventory.entity;

import com.joven.inventory.enums.ShiftStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a cashier shift.
 * Maps to the shifts table.
 * Tracks cash management from shift open to close with reconciliation.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Entity
@Table(name = "shifts")
@Getter
@Setter
@NoArgsConstructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cashier", nullable = false, length = 50)
    private String cashier;

    @Column(name = "opening_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal openingAmount = BigDecimal.ZERO;

    @Column(name = "closing_amount", precision = 12, scale = 2)
    private BigDecimal closingAmount;

    @Column(name = "expected_amount", precision = 12, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "difference", precision = 12, scale = 2)
    private BigDecimal difference;

    @Column(name = "total_sales", precision = 12, scale = 2)
    private BigDecimal totalSales;

    @Column(name = "total_transactions")
    private Integer totalTransactions;

    @Column(name = "total_voided", precision = 12, scale = 2)
    private BigDecimal totalVoided;

    @Column(name = "total_returns", precision = 12, scale = 2)
    private BigDecimal totalReturns;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShiftStatus status = ShiftStatus.OPEN;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
