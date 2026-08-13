package com.joven.inventory.service;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CloseShiftRequest;
import com.joven.inventory.dto.request.OpenShiftRequest;
import com.joven.inventory.dto.response.ShiftResponse;
import com.joven.inventory.dto.response.ShiftSummaryResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for cashier shift management.
 * Handles opening and closing of shifts with cash reconciliation and sales summaries.
 *
 * <p>Each cashier can only have one open shift at a time. Closing a shift calculates
 * totals, expected amounts, and payment method breakdowns.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface ShiftService {

    /**
     * Opens a new shift for the current user.
     * Validates that the user does not already have an open shift.
     *
     * @param request the open shift request containing the opening amount
     * @return the opened shift response
     * @throws com.joven.inventory.exception.BusinessRuleException if the user already has an open shift
     */
    ShiftResponse openShift(OpenShiftRequest request);

    /**
     * Closes an open shift with reconciliation.
     * Calculates total sales, total transactions, voided amounts, expected cash amount,
     * and the difference between expected and actual closing amounts.
     * Includes payment method breakdowns.
     *
     * @param shiftId the ID of the shift to close
     * @param request the close shift request containing the closing amount and optional remarks
     * @return the shift summary response with full sales breakdown
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the shift is not found
     * @throws com.joven.inventory.exception.BusinessRuleException     if the shift is not OPEN or does not belong to the current user
     */
    ShiftSummaryResponse closeShift(Long shiftId, CloseShiftRequest request);

    /**
     * Retrieves the current open shift for the authenticated user.
     *
     * @return the current open shift response
     * @throws com.joven.inventory.exception.ResourceNotFoundException if no open shift exists for the current user
     */
    ShiftResponse getCurrentShift();

    /**
     * Retrieves a shift by its ID.
     *
     * @param id the shift ID
     * @return the shift response
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the shift is not found
     */
    ShiftResponse getById(Long id);

    /**
     * Retrieves all shifts with pagination.
     *
     * @param pageable pagination information
     * @return a paginated response of shifts
     */
    PageResponse<ShiftResponse> getAll(Pageable pageable);
}
