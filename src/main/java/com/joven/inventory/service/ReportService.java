package com.joven.inventory.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service interface for generating PDF reports using JasperReports.
 * Provides methods for inventory, financial, and POS report generation.
 * All methods return the generated PDF as a byte array.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface ReportService {

    // ==================== Inventory Reports ====================

    /**
     * Generates a stock level report showing current inventory quantities.
     *
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the generated PDF report as a byte array
     */
    byte[] generateStockLevelReport(Long categoryId);

    /**
     * Generates a stock movement report showing inventory transactions within a date range.
     *
     * @param from       the start date/time (inclusive)
     * @param to         the end date/time (inclusive)
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the generated PDF report as a byte array
     */
    byte[] generateStockMovementReport(LocalDateTime from, LocalDateTime to, Long categoryId);

    /**
     * Generates a low stock report showing items at or below their reorder level.
     *
     * @return the generated PDF report as a byte array
     */
    byte[] generateLowStockReport();

    /**
     * Generates a transaction summary report within a date range, optionally filtered by type.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @param type the transaction type filter: ALL, IN, OUT, or ADJUSTMENT
     * @return the generated PDF report as a byte array
     */
    byte[] generateTransactionSummaryReport(LocalDateTime from, LocalDateTime to, String type);

    /**
     * Generates an item list report showing all active items.
     *
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the generated PDF report as a byte array
     */
    byte[] generateItemListReport(Long categoryId);

    /**
     * Generates an inventory count report for physical stock verification.
     *
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the generated PDF report as a byte array
     */
    byte[] generateInventoryCountReport(Long categoryId);

    /**
     * Generates a stock valuation report showing the monetary value of current inventory.
     *
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the generated PDF report as a byte array
     */
    byte[] generateStockValuationReport(Long categoryId);

    // ==================== Financial Reports ====================

    /**
     * Generates a gross profit report showing revenue, cost, and profit margins.
     *
     * @param from       the start date/time (inclusive)
     * @param to         the end date/time (inclusive)
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the generated PDF report as a byte array
     */
    byte[] generateGrossProfitReport(LocalDateTime from, LocalDateTime to, Long categoryId);

    /**
     * Generates a profit share report grouped by item or category.
     *
     * @param from    the start date/time (inclusive)
     * @param to      the end date/time (inclusive)
     * @param groupBy the grouping criteria: ITEM or CATEGORY
     * @return the generated PDF report as a byte array
     */
    byte[] generateProfitShareReport(LocalDateTime from, LocalDateTime to, String groupBy);

    /**
     * Generates a sales summary report showing total sales, discounts, and net revenue.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @return the generated PDF report as a byte array
     */
    byte[] generateSalesSummaryReport(LocalDateTime from, LocalDateTime to);

    /**
     * Generates a purchase summary report showing stock-in transactions.
     *
     * @param from       the start date/time (inclusive)
     * @param to         the end date/time (inclusive)
     * @param supplierId the supplier ID to filter by, or {@code null} for all suppliers
     * @return the generated PDF report as a byte array
     */
    byte[] generatePurchaseSummaryReport(LocalDateTime from, LocalDateTime to, Long supplierId);

    /**
     * Generates a VAT summary report showing taxable sales, tax collected, and input VAT.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @return the generated PDF report as a byte array
     */
    byte[] generateVatSummaryReport(LocalDateTime from, LocalDateTime to);

    // ==================== POS Reports ====================

    /**
     * Generates a daily sales report for a specific date.
     *
     * @param date the date to generate the report for
     * @return the generated PDF report as a byte array
     */
    byte[] generateDailySalesReport(LocalDate date);

    /**
     * Generates a shift report showing sales activity for a specific shift.
     *
     * @param shiftId the shift ID
     * @return the generated PDF report as a byte array
     */
    byte[] generateShiftReport(Long shiftId);

    /**
     * Generates a sales breakdown report grouped by payment method.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @return the generated PDF report as a byte array
     */
    byte[] generateSalesByPaymentReport(LocalDateTime from, LocalDateTime to);

    /**
     * Generates a sales report grouped by cashier.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @return the generated PDF report as a byte array
     */
    byte[] generateSalesByCashierReport(LocalDateTime from, LocalDateTime to);

    /**
     * Generates a top-selling items report within a date range.
     *
     * @param from  the start date/time (inclusive)
     * @param to    the end date/time (inclusive)
     * @param limit the maximum number of items to include
     * @return the generated PDF report as a byte array
     */
    byte[] generateTopSellingReport(LocalDateTime from, LocalDateTime to, int limit);

    /**
     * Generates a report of voided transactions within a date range.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @return the generated PDF report as a byte array
     */
    byte[] generateVoidedTransactionsReport(LocalDateTime from, LocalDateTime to);

    /**
     * Generates an hourly sales breakdown report for a specific date.
     *
     * @param date the date to generate the report for
     * @return the generated PDF report as a byte array
     */
    byte[] generateHourlySalesReport(LocalDate date);
}
