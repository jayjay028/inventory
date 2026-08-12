package com.joven.inventory.security;

import com.joven.inventory.entity.User;
import com.joven.inventory.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 * Loads user-specific data from the database via {@link UserRepository}
 * and converts it to a {@link CustomUserDetails} instance for authentication.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    /** Repository for accessing user data from the database */
    private final UserRepository userRepository;

    /**
     * Constructs the service with the required user repository.
     *
     * @param userRepository the repository for user data access
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their username for Spring Security authentication.
     * The user must exist in the database and have an active status.
     *
     * @param username the username identifying the user whose data is required
     * @return a fully populated {@link UserDetails} instance
     * @throws UsernameNotFoundException if the user is not found or is inactive
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException(
                            "User not found with username: " + username);
                });

        if (!Boolean.TRUE.equals(user.getActive())) {
            log.warn("User account is inactive: {}", username);
            throw new UsernameNotFoundException(
                    "User account is inactive: " + username);
        }

        log.debug("User loaded successfully: {}", username);
        return CustomUserDetails.fromUser(user);
    }
}
