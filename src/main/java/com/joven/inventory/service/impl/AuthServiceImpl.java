package com.joven.inventory.service.impl;

import com.joven.inventory.dto.request.LoginRequest;
import com.joven.inventory.dto.request.RefreshTokenRequest;
import com.joven.inventory.dto.response.LoginResponse;
import com.joven.inventory.entity.User;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.exception.UnauthorizedException;
import com.joven.inventory.repository.UserRepository;
import com.joven.inventory.security.CustomUserDetails;
import com.joven.inventory.security.JwtTokenProvider;
import com.joven.inventory.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of {@link AuthService} providing authentication operations
 * including login, token refresh, and current user retrieval.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     * <p>
     * Authenticates the user via Spring Security's {@link AuthenticationManager},
     * generates JWT access and refresh tokens, updates the user's last login timestamp,
     * and returns the authentication response.
     * </p>
     *
     * @param request the login request containing username and password
     * @return the login response containing JWT tokens and user information
     * @throws UnauthorizedException if authentication fails due to invalid credentials
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            // Update last login timestamp
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            log.info("User '{}' logged in successfully", userDetails.getUsername());

            return buildLoginResponse(accessToken, refreshToken, user);

        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for user '{}': {}", request.getUsername(), ex.getMessage());
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Validates the provided refresh token, extracts the username, loads the user
     * from the database, and generates new access and refresh tokens.
     * </p>
     *
     * @param request the refresh token request containing the refresh token
     * @return the login response containing new JWT tokens and user information
     * @throws UnauthorizedException if the refresh token is invalid or expired
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.getActive()) {
            throw new UnauthorizedException("User account is deactivated");
        }

        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        log.info("Token refreshed for user '{}'", username);

        return buildLoginResponse(newAccessToken, newRefreshToken, user);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Loads the user from the database by username and maps it to a {@link LoginResponse.UserInfo} DTO.
     * </p>
     *
     * @param username the username of the current authenticated user
     * @return the user information DTO
     * @throws ResourceNotFoundException if the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponse.UserInfo getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildUserInfo(user);
    }

    /**
     * Builds a complete login response with tokens and user information.
     *
     * @param accessToken  the JWT access token
     * @param refreshToken the JWT refresh token
     * @param user         the authenticated user entity
     * @return the constructed login response
     */
    private LoginResponse buildLoginResponse(String accessToken, String refreshToken, User user) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(1800L)
                .user(buildUserInfo(user))
                .build();
    }

    /**
     * Maps a User entity to a UserInfo DTO.
     *
     * @param user the user entity
     * @return the user info DTO
     */
    private LoginResponse.UserInfo buildUserInfo(User user) {
        return LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accessRights(user.getAccessRights())
                .build();
    }
}
