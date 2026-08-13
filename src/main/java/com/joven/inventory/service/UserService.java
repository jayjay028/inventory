package com.joven.inventory.service;

import com.joven.inventory.dto.request.PasswordResetRequest;
import com.joven.inventory.dto.request.UserRequest;
import com.joven.inventory.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for user management operations.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface UserService {

    /**
     * Retrieves all users with pagination.
     *
     * @param pageable pagination information
     * @return a page of user responses
     */
    Page<UserResponse> getAll(Pageable pageable);

    /**
     * Retrieves a single user by their ID.
     *
     * @param id the user ID
     * @return the user response
     */
    UserResponse getById(Long id);

    /**
     * Creates a new user with encoded password.
     *
     * @param request the user creation request
     * @return the created user response
     */
    UserResponse create(UserRequest request);

    /**
     * Updates an existing user. Password is only updated if provided.
     *
     * @param id      the user ID
     * @param request the user update request
     * @return the updated user response
     */
    UserResponse update(Long id, UserRequest request);

    /**
     * Activates or deactivates a user account.
     *
     * @param id     the user ID
     * @param active the desired active status
     * @return the updated user response
     */
    UserResponse updateStatus(Long id, boolean active);

    /**
     * Resets a user's password.
     *
     * @param id      the user ID
     * @param request the password reset request containing the new password
     * @return the updated user response
     */
    UserResponse resetPassword(Long id, PasswordResetRequest request);
}
