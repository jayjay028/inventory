package com.joven.inventory.security;

import com.joven.inventory.entity.User;
import com.joven.inventory.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of Spring Security's {@link UserDetails} interface.
 * Wraps the application's {@link User} entity to integrate with the
 * Spring Security authentication and authorization framework.
 *
 * <p>Includes additional fields such as access rights and role for
 * fine-grained permission evaluation beyond standard Spring Security authorities.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    /** Unique identifier of the user */
    private final Long id;

    /** Login username */
    private final String username;

    /** Encoded password */
    private final String password;

    /** Display name of the user */
    private final String fullName;

    /** Role assigned to the user (ADMIN or STAFF) */
    private final UserRole role;

    /** Bitwise access rights mask for granular permission control */
    private final Long accessRights;

    /** Whether the user account is active */
    private final Boolean active;

    /**
     * Constructs a CustomUserDetails with all required fields.
     *
     * @param id           the user's unique identifier
     * @param username     the user's login username
     * @param password     the user's encoded password
     * @param fullName     the user's display name
     * @param role         the user's role
     * @param accessRights the user's bitwise access rights
     * @param active       whether the user account is active
     */
    public CustomUserDetails(Long id, String username, String password, String fullName,
                             UserRole role, Long accessRights, Boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.accessRights = accessRights;
        this.active = active;
    }

    /**
     * Factory method to create a {@link CustomUserDetails} from a {@link User} entity.
     *
     * @param user the user entity to convert
     * @return a new CustomUserDetails instance populated from the entity
     */
    public static CustomUserDetails fromUser(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getFullName(),
                user.getRole(),
                user.getAccessRights(),
                user.getActive()
        );
    }

    /**
     * Returns the authorities granted to the user. Maps the user's role
     * to a Spring Security {@link GrantedAuthority} with the ROLE_ prefix.
     *
     * @return a collection containing the user's role as a granted authority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = "ROLE_" + role.name();
        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    /**
     * Returns the encoded password used to authenticate the user.
     *
     * @return the user's encoded password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user.
     *
     * @return the user's username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has expired.
     * This implementation always returns true (account never expires).
     *
     * @return true indicating the account is non-expired
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     * This implementation always returns true (account is never locked).
     *
     * @return true indicating the account is non-locked
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials have expired.
     * This implementation always returns true (credentials never expire).
     *
     * @return true indicating the credentials are non-expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * Maps directly to the user's active status.
     *
     * @return true if the user account is active
     */
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(active);
    }
}
