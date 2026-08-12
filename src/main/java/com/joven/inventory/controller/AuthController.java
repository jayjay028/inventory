package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.dto.request.LoginRequest;
import com.joven.inventory.dto.request.RefreshTokenRequest;
import com.joven.inventory.dto.response.LoginResponse;
import com.joven.inventory.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations.
 * Provides endpoints for login, token refresh, logout, and current user retrieval.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user with username and password credentials.
     *
     * @param request the login request containing username and password
     * @return the API response containing JWT tokens and user information
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Refreshes an expired access token using a valid refresh token.
     *
     * @param request the refresh token request containing the refresh token
     * @return the API response containing new JWT tokens and user information
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    /**
     * Logs out the current user.
     * Since the system uses stateless JWT authentication, this endpoint
     * simply acknowledges the logout request. Token invalidation is handled client-side.
     *
     * @return the API response confirming logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    /**
     * Retrieves the current authenticated user's information.
     *
     * @param authentication the Spring Security authentication object
     * @return the API response containing the current user's information
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> getCurrentUser(Authentication authentication) {
        LoginResponse.UserInfo userInfo = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved", userInfo));
    }
}
