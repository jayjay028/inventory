package com.joven.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO containing aggregated dashboard data for the inventory and POS system.
 * Includes summary metrics, recent activity, alerts, and top-selling items.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    /** Count of active items in the system */
    private Long totalItems;

    /** Count of active categories */
    private Long totalCategories;

    /** Sum of (stock.quantityOnHand * item.costPrice) for all items */
    private BigDecimal totalStockValue;

    /** Count of items at or below their reorder level */
    private Long lowStockCount;

    /** Count of PAID/CLOSED sales today */
    private Long todaySalesCount;

    /** Sum of totalAmount for PAID/CLOSED sales today */
    private BigDecimal todaySalesAmount;

    /** Today's gross profit (sales revenue minus cost) */
    private BigDecimal todayProfit;

    /** Count of PAID/CLOSED sales this month */
    private Long monthSalesCount;

    /** Sum of totalAmount for PAID/CLOSED sales this month */
    private BigDecimal monthSalesAmount;

    /** Count of stock transactions with CREATED status (pending approval) */
    private Long pendingApprovals;

    /** Last 10 stock transactions */
    private List<RecentTransaction> recentTransactions;

    /** Items currently at or below their reorder level */
    private List<LowStockAlert> lowStockAlerts;

    /** Top 5 items this month by quantity sold */
    private List<TopSellingItem> topSellingItems;

    /**
     * Represents a recent stock transaction for the dashboard.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTransaction {

        private Long id;
        private String itemName;
        private String transactionType;
        private Integer quantity;
        private String status;
        private BigDecimal totalAmount;
        private String createdBy;
        private LocalDateTime createdAt;
    }

    /**
     * Represents a low stock alert for an item at or below its reorder level.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockAlert {

        private Long itemId;
        private String itemCode;
        private String itemName;
        private String categoryName;
        private Integer quantityOnHand;
        private Integer reorderLevel;
    }

    /**
     * Represents a top-selling item by quantity sold this month.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSellingItem {

        private Long itemId;
        private String itemName;
        private Long totalQuantitySold;
        private BigDecimal totalRevenue;
    }
}
