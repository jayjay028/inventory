package com.joven.inventory.service.impl;

import com.joven.inventory.dto.response.AuditTrailResponse;
import com.joven.inventory.entity.AuditTrail;
import com.joven.inventory.enums.AuditAction;
import com.joven.inventory.repository.AuditTrailRepository;
import com.joven.inventory.service.AuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link AuditTrailService} providing read-only access
 * to audit trail records stored in the database.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditTrailServiceImpl implements AuditTrailService {

    private final AuditTrailRepository auditTrailRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<AuditTrailResponse> getAll(Pageable pageable) {
        return auditTrailRepository.findAll(pageable)
                .map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<AuditTrailResponse> getByEntity(String entityName, Long entityId, Pageable pageable) {
        return auditTrailRepository.findByEntityNameAndEntityId(entityName, entityId, pageable)
                .map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<AuditTrailResponse> getByEntityName(String entityName, Pageable pageable) {
        return auditTrailRepository.findByEntityName(entityName, pageable)
                .map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<AuditTrailResponse> getByPerformedBy(String performedBy, Pageable pageable) {
        return auditTrailRepository.findByPerformedBy(performedBy, pageable)
                .map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<AuditTrailResponse> getByAction(String action, Pageable pageable) {
        AuditAction auditAction = AuditAction.valueOf(action.toUpperCase());
        return auditTrailRepository.findByAction(auditAction, pageable)
                .map(this::toResponse);
    }

    /**
     * Converts an AuditTrail entity to an AuditTrailResponse DTO.
     *
     * @param entity the audit trail entity
     * @return the audit trail response DTO
     */
    private AuditTrailResponse toResponse(AuditTrail entity) {
        return AuditTrailResponse.builder()
                .id(entity.getId())
                .entityName(entity.getEntityName())
                .entityId(entity.getEntityId())
                .action(entity.getAction().name())
                .fieldName(entity.getFieldName())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .performedBy(entity.getPerformedBy())
                .performedAt(entity.getPerformedAt())
                .ipAddress(entity.getIpAddress())
                .build();
    }
}
