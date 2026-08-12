package com.joven.inventory.repository;

import com.joven.inventory.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link User} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if a user exists with the given username.
     *
     * @param username the username to check
     * @return true if a user with the username exists
     */
    boolean existsByUsername(String username);

    /**
     * Finds all active users with pagination.
     *
     * @param pageable pagination information
     * @return a page of active users
     */
    Page<User> findByActiveTrue(Pageable pageable);
}
