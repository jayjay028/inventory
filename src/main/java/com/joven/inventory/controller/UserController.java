package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.PasswordResetRequest;
import com.joven.inventory.dto.request.UserRequest;
import com.joven.inventory.dto.response.UserResponse;
import com.joven.inventory.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user management.
 * Provides endpoints for CRUD operations, status management, and password reset.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves all users with pagination.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return the API response containing a page of users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAll(Pageable pageable) {
        Page<UserResponse> page = userService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", PageResponse.of(page)));
    }

    /**
     * Retrieves a single user by their ID.
     *
     * @param id the user ID
     * @return the API response containing the user
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        UserResponse user = userService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    /**
     * Creates a new user.
     *
     * @param request the user creation request
     * @return the API response containing the created user
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest request) {
        UserResponse user = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    /**
     * Updates an existing user.
     *
     * @param id      the user ID
     * @param request the user update request
     * @return the API response containing the updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id,
                                                            @Valid @RequestBody UserRequest request) {
        UserResponse user = userService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    /**
     * Updates the active status of a user.
     *
     * @param id     the user ID
     * @param active the desired active status
     * @return the API response containing the updated user
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(@PathVariable Long id,
                                                                  @RequestParam boolean active) {
        UserResponse user = userService.updateStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", user));
    }

    /**
     * Resets a user's password.
     *
     * @param id      the user ID
     * @param request the password reset request containing the new password
     * @return the API response containing the updated user
     */
    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<UserResponse>> resetPassword(@PathVariable Long id,
                                                                   @Valid @RequestBody PasswordResetRequest request) {
        UserResponse user = userService.resetPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", user));
    }
}
