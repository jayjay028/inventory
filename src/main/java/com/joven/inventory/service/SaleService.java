package com.joven.inventory.service;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CreateSaleRequest;
import com.joven.inventory.dto.request.ProcessPaymentRequest;
import com.joven.inventory.dto.request.SaleItemRequest;
import com.joven.inventory.dto.request.VoidSaleRequest;
import com.joven.inventory.dto.response.ReceiptResponse;
import com.joven.inventory.dto.response.SaleDetailResponse;
import com.joven.inventory.dto.response.SaleResponse;
import com.joven.inventory.enums.SaleStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for POS sale operations.
 * Handles the full sale lifecycle: OPEN → PAID → CLOSED (or VOIDED at any stage).
 *
 * <p>Stock is deducted when payment is processed (OPEN → PAID transition).
 * If a PAID or CLOSED sale is voided, stock is reversed.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface SaleService {

    /**
     * Creates a new sale with status OPEN.
     * Validates the current user has an open shift, generates a sale number,
     * processes line items with discount calculations, and computes tax if enabled.
     *
     * @param request the sale creation request containing items, addons, discount, and tax settings
     * @return the created sale detail response
     * @throws com.joven.inventory.exception.BusinessRuleException     if no open shift exists for the current user
     * @throws com.joven.inventory.exception.ResourceNotFoundException if an item or customer is not found
     */
    SaleDetailResponse createSale(CreateSaleRequest request);

    /**
     * Updates the cart items on an OPEN sale.
     * Deletes existing line items and replaces them with the new list,
     * recalculating all financial totals.
     *
     * @param saleId the ID of the sale to update
     * @param items  the new list of sale items
     * @return the updated sale detail response
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the sale is not found
     * @throws com.joven.inventory.exception.BusinessRuleException     if the sale is not in OPEN status
     */
    SaleDetailResponse updateItems(Long saleId, List<SaleItemRequest> items);

    /**
     * Processes payment for an OPEN sale, transitioning it to PAID status.
     * Validates payment amount covers the total, creates payment records,
     * generates a document number if applicable, and deducts stock for all line items.
     *
     * @param saleId  the ID of the sale to pay
     * @param request the payment request containing method, amount, and optional split payments
     * @return the paid sale detail response
     * @throws com.joven.inventory.exception.ResourceNotFoundException   if the sale is not found
     * @throws com.joven.inventory.exception.BusinessRuleException       if the sale is not in OPEN status or payment is insufficient
     * @throws com.joven.inventory.exception.InsufficientStockException if stock is insufficient for any item
     */
    SaleDetailResponse processPayment(Long saleId, ProcessPaymentRequest request);

    /**
     * Closes a PAID sale, transitioning it to CLOSED status.
     *
     * @param saleId the ID of the sale to close
     * @return the closed sale detail response
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the sale is not found
     * @throws com.joven.inventory.exception.BusinessRuleException     if the sale is not in PAID status
     */
    SaleDetailResponse closeSale(Long saleId);

    /**
     * Voids a sale, transitioning it to VOIDED status.
     * If the sale was PAID or CLOSED, stock is reversed for all line items.
     *
     * @param saleId  the ID of the sale to void
     * @param request the void request containing the reason
     * @return the voided sale detail response
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the sale is not found
     * @throws com.joven.inventory.exception.BusinessRuleException     if the sale is already VOIDED
     */
    SaleDetailResponse voidSale(Long saleId, VoidSaleRequest request);

    /**
     * Retrieves a sale by its ID with full detail including items, addons, and payments.
     *
     * @param id the sale ID
     * @return the sale detail response
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the sale is not found
     */
    SaleDetailResponse getById(Long id);

    /**
     * Retrieves all sales with pagination.
     *
     * @param pageable pagination information
     * @return a paginated response of sales
     */
    PageResponse<SaleResponse> getAll(Pageable pageable);

    /**
     * Retrieves sales filtered by status with pagination.
     *
     * @param status   the sale status to filter by
     * @param pageable pagination information
     * @return a paginated response of sales with the given status
     */
    PageResponse<SaleResponse> getByStatus(SaleStatus status, Pageable pageable);

    /**
     * Retrieves sales within a date range with pagination.
     *
     * @param from     the start date (inclusive)
     * @param to       the end date (inclusive)
     * @param pageable pagination information
     * @return a paginated response of sales within the date range
     */
    PageResponse<SaleResponse> getByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable);

    /**
     * Retrieves receipt data for a PAID or CLOSED sale.
     * Includes business settings for receipt header/footer formatting.
     *
     * @param saleId the sale ID
     * @return the receipt response formatted for printing
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the sale is not found
     * @throws com.joven.inventory.exception.BusinessRuleException     if the sale is not in PAID or CLOSED status
     */
    ReceiptResponse getReceipt(Long saleId);

    /**
     * Retrieves all currently OPEN sales for the resume sale feature.
     *
     * @return a list of open sale responses
     */
    List<SaleResponse> getOpenSales();
}
