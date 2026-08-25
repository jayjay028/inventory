package com.joven.inventory.entity;

import com.joven.inventory.common.BaseEntity;
import com.joven.inventory.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a system user. Maps to the users table.
 * Stores authentication credentials, role, and bitwise access rights.
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    /** Bitwise access rights mask for granular permission control */
    @Column(name = "access_rights", nullable = false)
    private Long accessRights = 0L;

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
}
