package com.joven.inventory.audit;

import com.joven.inventory.common.BaseEntity;
import com.joven.inventory.entity.AuditTrail;
import com.joven.inventory.enums.AuditAction;
import com.joven.inventory.repository.AuditTrailRepository;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.lang.reflect.Field;

/**
 * JPA Entity Listener that automatically records audit trail entries when entities
 * are persisted, updated, or removed. Uses AuditContext for current user and IP address.
 *
 * <p>This listener performs two functions:</p>
 * <ul>
 *   <li>Sets {@code createdBy} and {@code updatedBy} on {@link BaseEntity} subclasses
 *       during pre-persist and pre-update callbacks.</li>
 *   <li>Records audit trail entries via {@link AuditTrailRepository} during post-persist,
 *       post-update, and post-remove callbacks.</li>
 * </ul>
 *
 * <p>Spring dependency injection is achieved through {@link SpringContext} since JPA
 * entity listeners are not managed by the Spring container directly.</p>
 *
 * <p><strong>Note:</strong> Field-level change tracking for updates is planned for Phase 2
 * using Hibernate Envers or manual comparison. Currently, update events record that an
 * update occurred without detailed field-level old/new values.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public class AuditEntityListener {

    /**
     * Sets the {@code createdBy} and {@code updatedBy} fields on BaseEntity subclasses
     * before the entity is persisted for the first time.
     *
     * @param entity the entity being persisted
     */
    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            String currentUser = AuditContext.getCurrentUser();
            baseEntity.setCreatedBy(currentUser);
            baseEntity.setUpdatedBy(currentUser);
        }
    }

    /**
     * Sets the {@code updatedBy} field on BaseEntity subclasses before the entity is updated.
     *
     * @param entity the entity being updated
     */
    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            String currentUser = AuditContext.getCurrentUser();
            baseEntity.setUpdatedBy(currentUser);
        }
    }

    /**
     * Records a CREATE audit trail entry after the entity has been persisted.
     *
     * @param entity the entity that was persisted
     */
    @PostPersist
    public void postPersist(Object entity) {
        recordAudit(entity, AuditAction.CREATE);
    }

    /**
     * Records an UPDATE audit trail entry after the entity has been updated.
     * <p>Note: Detailed field-level change tracking is planned for Phase 2.</p>
     *
     * @param entity the entity that was updated
     */
    @PostUpdate
    public void postUpdate(Object entity) {
        recordAudit(entity, AuditAction.UPDATE);
    }

    /**
     * Records a DELETE audit trail entry after the entity has been removed.
     *
     * @param entity the entity that was removed
     */
    @PostRemove
    public void postRemove(Object entity) {
        recordAudit(entity, AuditAction.DELETE);
    }

    /**
     * Creates and saves an AuditTrail record for the given entity and action.
     * Skips recording if the entity is an AuditTrail itself to prevent infinite recursion.
     *
     * @param entity the entity that triggered the audit event
     * @param action the audit action (CREATE, UPDATE, or DELETE)
     */
    private void recordAudit(Object entity, AuditAction action) {
        // Prevent infinite recursion — do not audit the AuditTrail entity itself
        if (entity instanceof AuditTrail) {
            return;
        }

        try {
            AuditTrailRepository repository = SpringContext.getBean(AuditTrailRepository.class);

            AuditTrail auditTrail = new AuditTrail();
            auditTrail.setEntityName(entity.getClass().getSimpleName());
            auditTrail.setEntityId(extractEntityId(entity));
            auditTrail.setAction(action);
            auditTrail.setPerformedBy(AuditContext.getCurrentUser());
            auditTrail.setIpAddress(AuditContext.getIpAddress());

            // For UPDATE actions, record a summary note in fieldName
            if (action == AuditAction.UPDATE) {
                auditTrail.setFieldName("entity");
                auditTrail.setNewValue("Entity updated");
            }

            repository.save(auditTrail);
        } catch (IllegalStateException e) {
            // ApplicationContext not yet initialized (e.g., during schema generation)
            // Silently skip audit recording in this case
        }
    }

    /**
     * Extracts the ID value from an entity using reflection.
     * Looks for a field named "id" in the entity class and its superclasses.
     *
     * @param entity the entity from which to extract the ID
     * @return the entity ID, or 0L if the ID field cannot be found or accessed
     */
    private Long extractEntityId(Object entity) {
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            try {
                Field idField = clazz.getDeclaredField("id");
                idField.setAccessible(true);
                Object idValue = idField.get(entity);
                if (idValue instanceof Long longId) {
                    return longId;
                }
                if (idValue instanceof Number numberId) {
                    return numberId.longValue();
                }
                return 0L;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                return 0L;
            }
        }
        return 0L;
    }
}
