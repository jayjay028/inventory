package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.dto.request.LoginRequest;
import com.joven.inventory.dto.request.RefreshTokenRequest;
import com.joven.inventory.dto.response.LoginResponse;
import com.joven.inventory.security.TokenDenyList;
import com.joven.inventory.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

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
    private final TokenDenyList tokenDenyList;

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
     * Logs out the current user by adding the token to the deny list.
     * The token will be rejected on subsequent requests until it naturally expires.
     *
     * @param authHeader the Authorization header containing the Bearer token
     * @return the API response confirming logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Deny for remaining token lifetime (max 30 min)
            tokenDenyList.denyToken(token, Instant.now().plusSeconds(1800));
        }
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
