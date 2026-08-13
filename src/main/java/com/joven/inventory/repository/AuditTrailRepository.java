package com.joven.inventory.repository;

import com.joven.inventory.entity.AuditTrail;
import com.joven.inventory.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for {@link AuditTrail} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {

    /**
     * Finds audit trail entries by entity name and entity ID.
     *
     * @param entityName the name of the entity
     * @param entityId the ID of the entity
     * @return a list of audit trail entries for the specified entity
     */
    List<AuditTrail> findByEntityNameAndEntityId(String entityName, Long entityId);

    /**
     * Finds audit trail entries by entity name and entity ID with pagination.
     *
     * @param entityName the name of the entity
     * @param entityId   the ID of the entity
     * @param pageable   pagination information
     * @return a page of audit trail entries for the specified entity
     */
    Page<AuditTrail> findByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);

    /**
     * Finds audit trail entries by entity name with pagination.
     *
     * @param entityName the name of the entity
     * @param pageable   pagination information
     * @return a page of audit trail entries for the specified entity name
     */
    Page<AuditTrail> findByEntityName(String entityName, Pageable pageable);

    /**
     * Finds audit trail entries by the user who performed the action, with pagination.
     *
     * @param username the username of the performer
     * @param pageable pagination information
     * @return a page of audit trail entries performed by the specified user
     */
    Page<AuditTrail> findByPerformedBy(String username, Pageable pageable);

    /**
     * Finds audit trail entries within a date range, with pagination.
     *
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @param pageable pagination information
     * @return a page of audit trail entries within the date range
     */
    Page<AuditTrail> findByPerformedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    /**
     * Finds audit trail entries by action type, with pagination.
     *
     * @param action the audit action type
     * @param pageable pagination information
     * @return a page of audit trail entries with the specified action
     */
    Page<AuditTrail> findByAction(AuditAction action, Pageable pageable);
}
