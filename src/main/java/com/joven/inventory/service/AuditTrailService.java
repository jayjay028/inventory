package com.joven.inventory.service;

import com.joven.inventory.dto.response.AuditTrailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for audit trail retrieval operations.
 * Provides read-only access to audit trail records for reporting and monitoring.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface AuditTrailService {

    /**
     * Retrieves all audit trail records with pagination.
     *
     * @param pageable pagination and sorting information
     * @return a page of audit trail responses
     */
    Page<AuditTrailResponse> getAll(Pageable pageable);

    /**
     * Retrieves audit trail records for a specific entity with pagination.
     *
     * @param entityName the name of the audited entity
     * @param entityId   the ID of the audited entity
     * @param pageable   pagination and sorting information
     * @return a page of audit trail responses for the specified entity
     */
    Page<AuditTrailResponse> getByEntity(String entityName, Long entityId, Pageable pageable);

    /**
     * Retrieves audit trail records filtered by entity name with pagination.
     *
     * @param entityName the name of the audited entity
     * @param pageable   pagination and sorting information
     * @return a page of audit trail responses filtered by entity name
     */
    Page<AuditTrailResponse> getByEntityName(String entityName, Pageable pageable);

    /**
     * Retrieves audit trail records filtered by the user who performed the action.
     *
     * @param performedBy the username of the performer
     * @param pageable    pagination and sorting information
     * @return a page of audit trail responses filtered by performer
     */
    Page<AuditTrailResponse> getByPerformedBy(String performedBy, Pageable pageable);

    /**
     * Retrieves audit trail records filtered by action type.
     *
     * @param action   the audit action type (CREATE, UPDATE, DELETE)
     * @param pageable pagination and sorting information
     * @return a page of audit trail responses filtered by action
     */
    Page<AuditTrailResponse> getByAction(String action, Pageable pageable);
}
