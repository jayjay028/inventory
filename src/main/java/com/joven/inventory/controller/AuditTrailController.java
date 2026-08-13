package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.dto.response.AuditTrailResponse;
import com.joven.inventory.service.AuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for audit trail retrieval.
 * Provides read-only endpoints for viewing audit trail records with filtering and pagination.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditTrailController {

    private final AuditTrailService auditTrailService;

    /**
     * Retrieves audit trail records with optional filtering and pagination.
     * Supports filtering by entity name, performed by, and action type.
     *
     * @param entityName  optional filter by entity name
     * @param performedBy optional filter by username who performed the action
     * @param action      optional filter by action type (CREATE, UPDATE, DELETE)
     * @param pageable    pagination and sorting parameters
     * @return the API response containing a page of audit trail records
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditTrailResponse>>> getAll(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String action,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        Page<AuditTrailResponse> result;

        if (entityName != null && !entityName.isBlank()) {
            result = auditTrailService.getByEntityName(entityName, pageable);
        } else if (performedBy != null && !performedBy.isBlank()) {
            result = auditTrailService.getByPerformedBy(performedBy, pageable);
        } else if (action != null && !action.isBlank()) {
            result = auditTrailService.getByAction(action, pageable);
        } else {
            result = auditTrailService.getAll(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success("Audit trail retrieved successfully", result));
    }

    /**
     * Retrieves audit trail records for a specific entity instance.
     *
     * @param name     the entity name
     * @param id       the entity ID
     * @param pageable pagination and sorting parameters
     * @return the API response containing a page of audit trail records for the entity
     */
    @GetMapping("/entity/{name}/{id}")
    public ResponseEntity<ApiResponse<Page<AuditTrailResponse>>> getByEntity(
            @PathVariable String name,
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        Page<AuditTrailResponse> result = auditTrailService.getByEntity(name, id, pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit trail retrieved successfully", result));
    }
}
