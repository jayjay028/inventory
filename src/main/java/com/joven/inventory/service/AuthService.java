package com.joven.inventory.service;

import com.joven.inventory.dto.request.LoginRequest;
import com.joven.inventory.dto.request.RefreshTokenRequest;
import com.joven.inventory.dto.response.LoginResponse;

/**
 * Service interface for authentication operations.
 * Handles login, token refresh, and current user retrieval.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface AuthService {

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param request the login request containing username and password
     * @return the login response containing JWT tokens and user information
     */
    LoginResponse login(LoginRequest request);

    /**
     * Refreshes an expired access token using a valid refresh token.
     *
     * @param request the refresh token request containing the refresh token
     * @return the login response containing new JWT tokens and user information
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * Retrieves the current authenticated user's information.
     *
     * @param username the username of the current authenticated user
     * @return the user information DTO
     */
    LoginResponse.UserInfo getCurrentUser(String username);
}
