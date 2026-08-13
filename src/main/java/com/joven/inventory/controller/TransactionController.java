package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.response.StockTransactionResponse;
import com.joven.inventory.service.StockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for stock transaction query operations.
 * Provides endpoints for listing and viewing stock transactions,
 * including pending transactions awaiting approval.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final StockTransactionService stockTransactionService;

    /**
     * Retrieves all stock transactions with pagination.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return the API response containing a page of stock transactions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StockTransactionResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<StockTransactionResponse> response = stockTransactionService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", response));
    }

    /**
     * Retrieves a stock transaction by its ID, including add-on details.
     *
     * @param id the transaction ID
     * @return the API response containing the stock transaction with add-ons
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> getById(@PathVariable Long id) {
        StockTransactionResponse response = stockTransactionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", response));
    }

    /**
     * Retrieves all pending (CREATED status) stock transactions with pagination.
     * These are transactions awaiting approval or cancellation.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return the API response containing a page of pending stock transactions
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PageResponse<StockTransactionResponse>>> getPending(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<StockTransactionResponse> response = stockTransactionService.getPending(pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending transactions retrieved successfully", response));
    }
}
