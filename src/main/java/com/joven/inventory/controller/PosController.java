package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CreateSaleRequest;
import com.joven.inventory.dto.request.ProcessPaymentRequest;
import com.joven.inventory.dto.request.SaleItemRequest;
import com.joven.inventory.dto.request.VoidSaleRequest;
import com.joven.inventory.dto.response.ItemResponse;
import com.joven.inventory.dto.response.ReceiptResponse;
import com.joven.inventory.dto.response.SaleDetailResponse;
import com.joven.inventory.dto.response.SaleResponse;
import com.joven.inventory.service.ItemService;
import com.joven.inventory.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * REST controller for Point of Sale operations.
 * Provides endpoints for managing sales lifecycle (create, pay, close, void),
 * retrieving sales data, generating receipts, and searching items for the POS terminal.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
public class PosController {

    private final SaleService saleService;
    private final ItemService itemService;

    /**
     * Creates a new sale with status OPEN.
     * Requires an active shift for the current user.
     *
     * @param request the sale creation request containing items, addons, discount, and tax settings
     * @return the API response containing the created sale detail
     */
    @PostMapping("/sales")
    public ResponseEntity<ApiResponse<SaleDetailResponse>> createSale(
            @Valid @RequestBody CreateSaleRequest request) {
        SaleDetailResponse response = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sale created successfully", response));
    }

    /**
     * Retrieves all sales with pagination.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return the API response containing a page of sales
     */
    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<SaleResponse> response = saleService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Sales retrieved successfully", response));
    }

    /**
     * Retrieves a sale by its ID with full detail including items, addons, and payments.
     *
     * @param id the sale ID
     * @return the API response containing the sale detail
     */
    @GetMapping("/sales/{id}")
    public ResponseEntity<ApiResponse<SaleDetailResponse>> getById(@PathVariable Long id) {
        SaleDetailResponse response = saleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Sale retrieved successfully", response));
    }

    /**
     * Updates the cart items on an OPEN sale.
     * Replaces existing line items with the new list and recalculates totals.
     *
     * @param id    the sale ID
     * @param items the new list of sale items
     * @return the API response containing the updated sale detail
     */
    @PutMapping("/sales/{id}/items")
    public ResponseEntity<ApiResponse<SaleDetailResponse>> updateItems(
            @PathVariable Long id,
            @Valid @RequestBody List<SaleItemRequest> items) {
        SaleDetailResponse response = saleService.updateItems(id, items);
        return ResponseEntity.ok(ApiResponse.success("Sale items updated successfully", response));
    }

    /**
     * Processes payment for an OPEN sale, transitioning it to PAID status.
     * Validates payment amount covers the sale total and deducts stock.
     *
     * @param id      the sale ID
     * @param request the payment request containing method, amount, and optional split payments
     * @return the API response containing the paid sale detail
     */
    @PostMapping("/sales/{id}/pay")
    public ResponseEntity<ApiResponse<SaleDetailResponse>> processPayment(
            @PathVariable Long id,
            @Valid @RequestBody ProcessPaymentRequest request) {
        SaleDetailResponse response = saleService.processPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", response));
    }

    /**
     * Closes a PAID sale, transitioning it to CLOSED status.
     *
     * @param id the sale ID
     * @return the API response containing the closed sale detail
     */
    @PatchMapping("/sales/{id}/close")
    public ResponseEntity<ApiResponse<SaleDetailResponse>> closeSale(@PathVariable Long id) {
        SaleDetailResponse response = saleService.closeSale(id);
        return ResponseEntity.ok(ApiResponse.success("Sale closed successfully", response));
    }

    /**
     * Voids a sale, transitioning it to VOIDED status.
     * If the sale was PAID or CLOSED, stock is reversed for all line items.
     *
     * @param id      the sale ID
     * @param request the void request containing the reason
     * @return the API response containing the voided sale detail
     */
    @PostMapping("/sales/{id}/void")
    public ResponseEntity<ApiResponse<SaleDetailResponse>> voidSale(
            @PathVariable Long id,
            @Valid @RequestBody VoidSaleRequest request) {
        SaleDetailResponse response = saleService.voidSale(id, request);
        return ResponseEntity.ok(ApiResponse.success("Sale voided successfully", response));
    }

    /**
     * Retrieves receipt data for a PAID or CLOSED sale.
     * Includes business settings for receipt header/footer formatting.
     *
     * @param id the sale ID
     * @return the API response containing the receipt data
     */
    @GetMapping("/sales/{id}/receipt")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getReceipt(@PathVariable Long id) {
        ReceiptResponse response = saleService.getReceipt(id);
        return ResponseEntity.ok(ApiResponse.success("Receipt generated successfully", response));
    }

    /**
     * Retrieves today's sales with pagination.
     * Filters sales from the start of today (00:00:00) to end of today (23:59:59).
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return the API response containing a page of today's sales
     */
    @GetMapping("/sales/today")
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> getTodaySales(
            @PageableDefault(size = 20) Pageable pageable) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        PageResponse<SaleResponse> response = saleService.getByDateRange(startOfDay, endOfDay, pageable);
        return ResponseEntity.ok(ApiResponse.success("Today's sales retrieved", response));
    }

    /**
     * Retrieves all currently OPEN sales for the resume sale feature.
     *
     * @return the API response containing the list of open sales
     */
    @GetMapping("/sales/open")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getOpenSales() {
        List<SaleResponse> response = saleService.getOpenSales();
        return ResponseEntity.ok(ApiResponse.success("Open sales retrieved", response));
    }

    /**
     * Searches items by name or item code for the POS terminal.
     * Returns active items matching the search query.
     *
     * @param q        the search query string
     * @param pageable pagination parameters (page, size, sort)
     * @return the API response containing a page of matching items
     */
    @GetMapping("/items/search")
    public ResponseEntity<ApiResponse<PageResponse<ItemResponse>>> searchItems(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<ItemResponse> response = itemService.search(q, pageable);
        return ResponseEntity.ok(ApiResponse.success("Items retrieved", response));
    }
}
