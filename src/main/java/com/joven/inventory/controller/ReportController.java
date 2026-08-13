package com.joven.inventory.controller;

import com.joven.inventory.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * REST controller for generating and serving PDF reports.
 * Provides endpoints for inventory, financial, and POS reports
 * using JasperReports via the {@link ReportService}.
 * All endpoints return raw PDF byte arrays with appropriate content headers.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ==================== Inventory Reports ====================

    /**
     * Generates a stock level report showing current inventory quantities.
     *
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/stock-level")
    public ResponseEntity<byte[]> getStockLevelReport(
            @RequestParam(required = false) Long categoryId) {

        byte[] pdfBytes = reportService.generateStockLevelReport(categoryId);
        String filename = "stock-level-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a stock movement report showing inventory transactions within a date range.
     *
     * @param from       the start date/time (inclusive)
     * @param to         the end date/time (inclusive)
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/stock-movement")
    public ResponseEntity<byte[]> getStockMovementReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long categoryId) {

        byte[] pdfBytes = reportService.generateStockMovementReport(from, to, categoryId);
        String filename = "stock-movement-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a low stock report showing items at or below their reorder level.
     *
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/low-stock")
    public ResponseEntity<byte[]> getLowStockReport() {

        byte[] pdfBytes = reportService.generateLowStockReport();
        String filename = "low-stock-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a transaction summary report within a date range, optionally filtered by type.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @param type the transaction type filter: ALL, IN, OUT, or ADJUSTMENT
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/transaction-summary")
    public ResponseEntity<byte[]> getTransactionSummaryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false, defaultValue = "ALL") String type) {

        byte[] pdfBytes = reportService.generateTransactionSummaryReport(from, to, type);
        String filename = "transaction-summary-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates an item list report showing all active items.
     *
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/item-list")
    public ResponseEntity<byte[]> getItemListReport(
            @RequestParam(required = false) Long categoryId) {

        byte[] pdfBytes = reportService.generateItemListReport(categoryId);
        String filename = "item-list-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates an inventory count report for physical stock verification.
     *
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/inventory-count")
    public ResponseEntity<byte[]> getInventoryCountReport(
            @RequestParam(required = false) Long categoryId) {

        byte[] pdfBytes = reportService.generateInventoryCountReport(categoryId);
        String filename = "inventory-count-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a stock valuation report showing the monetary value of current inventory.
     *
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/stock-valuation")
    public ResponseEntity<byte[]> getStockValuationReport(
            @RequestParam(required = false) Long categoryId) {

        byte[] pdfBytes = reportService.generateStockValuationReport(categoryId);
        String filename = "stock-valuation-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    // ==================== Financial Reports ====================

    /**
     * Generates a gross profit report showing revenue, cost, and profit margins.
     *
     * @param from       the start date/time (inclusive)
     * @param to         the end date/time (inclusive)
     * @param categoryId the category ID to filter by, or {@code null} for all categories
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/gross-profit")
    public ResponseEntity<byte[]> getGrossProfitReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long categoryId) {

        byte[] pdfBytes = reportService.generateGrossProfitReport(from, to, categoryId);
        String filename = "gross-profit-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a profit share report grouped by item or category.
     *
     * @param from    the start date/time (inclusive)
     * @param to      the end date/time (inclusive)
     * @param groupBy the grouping criteria: ITEM or CATEGORY
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/profit-share")
    public ResponseEntity<byte[]> getProfitShareReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false, defaultValue = "ITEM") String groupBy) {

        byte[] pdfBytes = reportService.generateProfitShareReport(from, to, groupBy);
        String filename = "profit-share-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a sales summary report showing total sales, discounts, and net revenue.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/sales-summary")
    public ResponseEntity<byte[]> getSalesSummaryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        byte[] pdfBytes = reportService.generateSalesSummaryReport(from, to);
        String filename = "sales-summary-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a purchase summary report showing stock-in transactions.
     *
     * @param from       the start date/time (inclusive)
     * @param to         the end date/time (inclusive)
     * @param supplierId the supplier ID to filter by, or {@code null} for all suppliers
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/purchase-summary")
    public ResponseEntity<byte[]> getPurchaseSummaryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long supplierId) {

        byte[] pdfBytes = reportService.generatePurchaseSummaryReport(from, to, supplierId);
        String filename = "purchase-summary-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a VAT summary report showing taxable sales, tax collected, and input VAT.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/vat-summary")
    public ResponseEntity<byte[]> getVatSummaryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        byte[] pdfBytes = reportService.generateVatSummaryReport(from, to);
        String filename = "vat-summary-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    // ==================== POS Reports ====================

    /**
     * Generates a daily sales report for a specific date.
     *
     * @param date the date to generate the report for
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/pos/daily-sales")
    public ResponseEntity<byte[]> getDailySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        byte[] pdfBytes = reportService.generateDailySalesReport(date);
        String filename = "daily-sales-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a shift report showing sales activity for a specific shift.
     *
     * @param shiftId the shift ID
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/pos/shift")
    public ResponseEntity<byte[]> getShiftReport(
            @RequestParam(required = false) Long shiftId) {

        byte[] pdfBytes = reportService.generateShiftReport(shiftId);
        String filename = "shift-report-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a sales breakdown report grouped by payment method.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/pos/sales-by-payment")
    public ResponseEntity<byte[]> getSalesByPaymentReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        byte[] pdfBytes = reportService.generateSalesByPaymentReport(from, to);
        String filename = "sales-by-payment-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a sales report grouped by cashier.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/pos/sales-by-cashier")
    public ResponseEntity<byte[]> getSalesByCashierReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        byte[] pdfBytes = reportService.generateSalesByCashierReport(from, to);
        String filename = "sales-by-cashier-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a top-selling items report within a date range.
     *
     * @param from  the start date/time (inclusive)
     * @param to    the end date/time (inclusive)
     * @param limit the maximum number of items to include (defaults to 20)
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/pos/top-selling")
    public ResponseEntity<byte[]> getTopSellingReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "20") int limit) {

        byte[] pdfBytes = reportService.generateTopSellingReport(from, to, limit);
        String filename = "top-selling-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates a report of voided transactions within a date range.
     *
     * @param from the start date/time (inclusive)
     * @param to   the end date/time (inclusive)
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/pos/voided")
    public ResponseEntity<byte[]> getVoidedTransactionsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        byte[] pdfBytes = reportService.generateVoidedTransactionsReport(from, to);
        String filename = "voided-transactions-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    /**
     * Generates an hourly sales breakdown report for a specific date.
     *
     * @param date the date to generate the report for
     * @return the PDF report as a byte array with appropriate headers
     */
    @GetMapping("/pos/hourly-sales")
    public ResponseEntity<byte[]> getHourlySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        byte[] pdfBytes = reportService.generateHourlySalesReport(date);
        String filename = "hourly-sales-" + LocalDate.now() + ".pdf";
        return buildPdfResponse(pdfBytes, filename);
    }

    // ==================== Helper Methods ====================

    /**
     * Builds a {@link ResponseEntity} with PDF content headers.
     *
     * @param pdfBytes the PDF content as a byte array
     * @param filename the filename to use in the Content-Disposition header
     * @return the response entity with PDF headers and body
     */
    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdfBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
