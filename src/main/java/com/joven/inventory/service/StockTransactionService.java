package com.joven.inventory.service;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.StockAdjustRequest;
import com.joven.inventory.dto.request.StockInRequest;
import com.joven.inventory.dto.request.StockOutRequest;
import com.joven.inventory.dto.response.StockTransactionResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Service interface for stock transaction operations.
 * Handles creation, approval, and cancellation of stock-in, stock-out,
 * and stock adjustment transactions following an approval workflow.
 *
 * <p>Transaction lifecycle: CREATED → APPROVED or CANCELLED.
 * Stock levels are only affected when a transaction is approved.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface StockTransactionService {

    /**
     * Creates a new stock-in transaction.
     * Calculates subtotal, processes add-ons, applies tax if enabled,
     * and generates a document number if a document type is specified.
     *
     * @param request the stock-in request containing item, quantity, cost, and optional details
     * @return the created stock transaction response
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the item or supplier is not found
     * @throws com.joven.inventory.exception.BusinessRuleException     if the item is inactive
     */
    StockTransactionResponse createStockIn(StockInRequest request);

    /**
     * Creates a new stock-out transaction.
     * Validates sufficient stock is available, calculates subtotal with discount,
     * processes add-ons, applies tax if enabled, and generates a document number if specified.
     *
     * <p>Note: Stock validation is performed at creation time, but actual deduction
     * only happens when the transaction is approved.</p>
     *
     * @param request the stock-out request containing item, quantity, price, discount, and optional details
     * @return the created stock transaction response
     * @throws com.joven.inventory.exception.ResourceNotFoundException   if the item or customer is not found
     * @throws com.joven.inventory.exception.BusinessRuleException       if the item is inactive
     * @throws com.joven.inventory.exception.InsufficientStockException if available stock is less than the requested quantity
     */
    StockTransactionResponse createStockOut(StockOutRequest request);

    /**
     * Creates a new stock adjustment transaction.
     * Records the target quantity with no financial calculations.
     *
     * @param request the stock adjustment request containing item and quantity
     * @return the created stock transaction response
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the item is not found
     * @throws com.joven.inventory.exception.BusinessRuleException     if the item is inactive
     */
    StockTransactionResponse createStockAdjust(StockAdjustRequest request);

    /**
     * Approves a pending stock transaction and updates stock levels accordingly.
     * <ul>
     *     <li>IN: Adds quantity to stock</li>
     *     <li>OUT: Deducts quantity from stock</li>
     *     <li>ADJUSTMENT: Sets stock to the specified quantity</li>
     * </ul>
     *
     * @param id the transaction ID to approve
     * @return the approved stock transaction response
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the transaction is not found
     * @throws com.joven.inventory.exception.BusinessRuleException     if the transaction is not in CREATED status
     */
    StockTransactionResponse approve(Long id);

    /**
     * Cancels a pending stock transaction without affecting stock levels.
     *
     * @param id the transaction ID to cancel
     * @return the cancelled stock transaction response
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the transaction is not found
     * @throws com.joven.inventory.exception.BusinessRuleException     if the transaction is not in CREATED status
     */
    StockTransactionResponse cancel(Long id);

    /**
     * Retrieves all stock transactions with pagination.
     *
     * @param pageable pagination information
     * @return a paginated response of stock transactions
     */
    PageResponse<StockTransactionResponse> getAll(Pageable pageable);

    /**
     * Retrieves a stock transaction by its ID, including add-on details.
     *
     * @param id the transaction ID
     * @return the stock transaction response with add-ons
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the transaction is not found
     */
    StockTransactionResponse getById(Long id);

    /**
     * Retrieves all pending (CREATED status) stock transactions with pagination.
     *
     * @param pageable pagination information
     * @return a paginated response of pending stock transactions
     */
    PageResponse<StockTransactionResponse> getPending(Pageable pageable);

    /**
     * Retrieves stock transactions within a date range with pagination.
     *
     * @param from     the start date (inclusive)
     * @param to       the end date (inclusive)
     * @param pageable pagination information
     * @return a paginated response of stock transactions within the date range
     */
    PageResponse<StockTransactionResponse> getByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
