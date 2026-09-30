package com.joven.inventory.entity;

import com.joven.inventory.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a user role. Maps to the roles table.
 *
 * <p>Roles are database-driven and manageable at runtime by administrators.
 * Each role carries a bitwise {@code accessRights} mask that defines the
 * permissions granted to users assigned to it. A user's effective permissions
 * are derived entirely from their role.</p>
 *
 * <p>Role ID 1 is the reserved system ADMIN role: it always holds all permissions
 * and cannot be deleted, deactivated, or have its system status changed.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role extends BaseEntity {

    /** Reserved ID of the built-in system ADMIN role. */
    public static final long ADMIN_ROLE_ID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    /** Bitwise access rights mask defining the permissions granted by this role. */
    @Column(name = "access_rights", nullable = false)
    private Long accessRights = 0L;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /** Whether this is a protected system role that cannot be deleted or altered structurally. */
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;
}
