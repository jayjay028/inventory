package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.AddonMasterRequest;
import com.joven.inventory.dto.response.AddonMasterResponse;
import com.joven.inventory.service.AddonMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import java.util.List;

/**
 * REST controller for add-on master management.
 * Provides endpoints for CRUD operations on add-on master records.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/addons")
@RequiredArgsConstructor
public class AddonMasterController {

    private final AddonMasterService addonMasterService;

    /**
     * Retrieves all add-on master records with pagination.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return the API response containing a page of add-on master records
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AddonMasterResponse>>> getAll(Pageable pageable) {
        Page<AddonMasterResponse> page = addonMasterService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Add-ons retrieved successfully", PageResponse.of(page)));
    }

    /**
     * Retrieves all active add-on master records.
     *
     * @return the API response containing a list of active add-on master records
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AddonMasterResponse>>> getAllActive() {
        List<AddonMasterResponse> addons = addonMasterService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success("Active add-ons retrieved successfully", addons));
    }

    /**
     * Retrieves a single add-on master record by its ID.
     *
     * @param id the add-on master ID
     * @return the API response containing the add-on master record
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddonMasterResponse>> getById(@PathVariable Long id) {
        AddonMasterResponse addon = addonMasterService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Add-on retrieved successfully", addon));
    }

    /**
     * Creates a new add-on master record.
     *
     * @param request the add-on master creation request
     * @return the API response containing the created add-on master record
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AddonMasterResponse>> create(@Valid @RequestBody AddonMasterRequest request) {
        AddonMasterResponse addon = addonMasterService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Add-on created successfully", addon));
    }

    /**
     * Updates an existing add-on master record.
     *
     * @param id      the add-on master ID
     * @param request the add-on master update request
     * @return the API response containing the updated add-on master record
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddonMasterResponse>> update(@PathVariable Long id,
                                                                   @Valid @RequestBody AddonMasterRequest request) {
        AddonMasterResponse addon = addonMasterService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Add-on updated successfully", addon));
    }

    /**
     * Updates the active status of an add-on master record.
     *
     * @param id     the add-on master ID
     * @param active the desired active status
     * @return the API response containing the updated add-on master record
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AddonMasterResponse>> updateStatus(@PathVariable Long id,
                                                                         @RequestParam boolean active) {
        AddonMasterResponse addon = addonMasterService.updateStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Add-on status updated successfully", addon));
    }
}
