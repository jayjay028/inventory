package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.StockAdjustRequest;
import com.joven.inventory.dto.request.StockInRequest;
import com.joven.inventory.dto.request.StockOutRequest;
import com.joven.inventory.dto.response.StockResponse;
import com.joven.inventory.dto.response.StockTransactionResponse;
import com.joven.inventory.service.StockService;
import com.joven.inventory.service.StockTransactionService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for stock management operations.
 * Provides endpoints for querying stock levels and creating/managing
 * stock transactions (in, out, adjustment) with an approval workflow.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockTransactionService stockTransactionService;

    /**
     * Retrieves all stock levels with pagination.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return the API response containing a page of stock records
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StockResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<StockResponse> response = stockService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Stock levels retrieved successfully", response));
    }

    /**
     * Retrieves the stock level for a specific item.
     *
     * @param itemId the item ID
     * @return the API response containing the stock data for the item
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<StockResponse>> getByItemId(@PathVariable Long itemId) {
        StockResponse response = stockService.getByItemId(itemId);
        return ResponseEntity.ok(ApiResponse.success("Stock retrieved successfully", response));
    }

    /**
     * Creates a new stock-in transaction.
     * The transaction is created with CREATED status and must be approved
     * before stock levels are updated.
     *
     * @param request the stock-in request containing item, quantity, cost, and optional details
     * @return the API response containing the created stock transaction
     */
    @PostMapping("/in")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> createStockIn(
            @Valid @RequestBody StockInRequest request) {
        StockTransactionResponse response = stockTransactionService.createStockIn(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock-in transaction created successfully", response));
    }

    /**
     * Creates a new stock-out transaction.
     * The transaction is created with CREATED status and must be approved
     * before stock levels are updated. Validates sufficient stock at creation time.
     *
     * @param request the stock-out request containing item, quantity, price, discount, and optional details
     * @return the API response containing the created stock transaction
     */
    @PostMapping("/out")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> createStockOut(
            @Valid @RequestBody StockOutRequest request) {
        StockTransactionResponse response = stockTransactionService.createStockOut(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock-out transaction created successfully", response));
    }

    /**
     * Creates a new stock adjustment transaction.
     * The transaction is created with CREATED status and must be approved
     * before stock levels are set to the specified quantity.
     *
     * @param request the stock adjustment request containing item and target quantity
     * @return the API response containing the created stock transaction
     */
    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> createStockAdjust(
            @Valid @RequestBody StockAdjustRequest request) {
        StockTransactionResponse response = stockTransactionService.createStockAdjust(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock adjustment transaction created successfully", response));
    }

    /**
     * Approves a pending stock transaction and updates stock levels accordingly.
     * Only transactions with CREATED status can be approved.
     *
     * @param id the transaction ID to approve
     * @return the API response containing the approved stock transaction
     */
    @PatchMapping("/transactions/{id}/approve")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> approve(@PathVariable Long id) {
        StockTransactionResponse response = stockTransactionService.approve(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction approved successfully", response));
    }

    /**
     * Cancels a pending stock transaction without affecting stock levels.
     * Only transactions with CREATED status can be cancelled.
     *
     * @param id the transaction ID to cancel
     * @return the API response containing the cancelled stock transaction
     */
    @PatchMapping("/transactions/{id}/cancel")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> cancel(@PathVariable Long id) {
        StockTransactionResponse response = stockTransactionService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction cancelled successfully", response));
    }
}
