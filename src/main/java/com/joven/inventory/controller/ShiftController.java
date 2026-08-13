package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CloseShiftRequest;
import com.joven.inventory.dto.request.OpenShiftRequest;
import com.joven.inventory.dto.response.ShiftResponse;
import com.joven.inventory.dto.response.ShiftSummaryResponse;
import com.joven.inventory.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for cashier shift management.
 * Provides endpoints for opening and closing shifts, retrieving current shift,
 * and querying shift history with pagination.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/pos/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    /**
     * Opens a new shift for the current user.
     * Each cashier can only have one open shift at a time.
     *
     * @param request the open shift request containing the opening amount
     * @return the API response containing the opened shift
     */
    @PostMapping("/open")
    public ResponseEntity<ApiResponse<ShiftResponse>> openShift(
            @Valid @RequestBody OpenShiftRequest request) {
        ShiftResponse response = shiftService.openShift(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shift opened successfully", response));
    }

    /**
     * Closes an open shift with cash reconciliation.
     * Calculates total sales, total transactions, voided amounts, expected cash amount,
     * and the difference between expected and actual closing amounts.
     *
     * @param id      the shift ID to close
     * @param request the close shift request containing the closing amount and optional remarks
     * @return the API response containing the shift summary with sales breakdown
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<ShiftSummaryResponse>> closeShift(
            @PathVariable Long id,
            @Valid @RequestBody CloseShiftRequest request) {
        ShiftSummaryResponse response = shiftService.closeShift(id, request);
        return ResponseEntity.ok(ApiResponse.success("Shift closed successfully", response));
    }

    /**
     * Retrieves the current open shift for the authenticated user.
     *
     * @return the API response containing the current open shift
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<ShiftResponse>> getCurrentShift() {
        ShiftResponse response = shiftService.getCurrentShift();
        return ResponseEntity.ok(ApiResponse.success("Current shift retrieved", response));
    }

    /**
     * Retrieves all shifts with pagination.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return the API response containing a page of shifts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ShiftResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<ShiftResponse> response = shiftService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Shifts retrieved successfully", response));
    }

    /**
     * Retrieves a shift by its ID.
     *
     * @param id the shift ID
     * @return the API response containing the shift data
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShiftResponse>> getById(@PathVariable Long id) {
        ShiftResponse response = shiftService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Shift retrieved successfully", response));
    }
}
