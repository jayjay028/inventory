package com.joven.inventory.entity;

import com.joven.inventory.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a system user. Maps to the users table.
 * Stores authentication credentials and a reference to the user's role,
 * which determines the user's permissions (access rights).
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", length = 150)
    private String email;

    /**
     * The role assigned to this user. The role determines the user's
     * effective permissions via its bitwise access rights.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     * The set of stores this user is granted access to. A user may operate
     * one or more stores. Managed via the user_stores join table.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_stores",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    private Set<Store> accessibleStores = new HashSet<>();

    /**
     * Convenience accessor for the user's effective access rights,
     * derived from the assigned role.
     *
     * @return the role's access rights, or 0 if no role is assigned
     */
    public Long getAccessRights() {
        return role != null ? role.getAccessRights() : 0L;
    }
}
