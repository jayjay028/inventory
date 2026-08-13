package com.joven.inventory.service.impl;

import com.joven.inventory.dto.request.LoginRequest;
import com.joven.inventory.dto.request.RefreshTokenRequest;
import com.joven.inventory.dto.response.LoginResponse;
import com.joven.inventory.entity.User;
import com.joven.inventory.enums.UserRole;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.exception.UnauthorizedException;
import com.joven.inventory.repository.UserRepository;
import com.joven.inventory.security.CustomUserDetails;
import com.joven.inventory.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthServiceImpl}.
 * Tests authentication, token refresh, and current user retrieval operations.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    // --- Helper Methods ---

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded_password");
        user.setFullName("Admin User");
        user.setEmail("admin@example.com");
        user.setRole(UserRole.ADMIN);
        user.setAccessRights(255L);
        user.setActive(true);
        return user;
    }

    private User createInactiveUser() {
        User user = createTestUser();
        user.setActive(false);
        return user;
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password123");
        return request;
    }

    private RefreshTokenRequest createRefreshTokenRequest(String token) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(token);
        return request;
    }

    // --- Login Tests ---

    @Test
    @DisplayName("login - given valid credentials - returns login response with tokens and user info")
    void login_givenValidCredentials_returnsLoginResponse() {
        // Arrange
        User user = createTestUser();
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        LoginRequest request = createLoginRequest();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access-token-123");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refresh-token-456");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(1800L);

        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getUsername()).isEqualTo("admin");
        assertThat(response.getUser().getFullName()).isEqualTo("Admin User");
        assertThat(response.getUser().getEmail()).isEqualTo("admin@example.com");
        assertThat(response.getUser().getRole()).isEqualTo("ADMIN");
        assertThat(response.getUser().getAccessRights()).isEqualTo(255L);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("admin");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("login - given invalid credentials - throws UnauthorizedException")
    void login_givenInvalidCredentials_throwsUnauthorizedException() {
        // Arrange
        LoginRequest request = createLoginRequest();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username or password");
    }

    // --- Refresh Token Tests ---

    @Test
    @DisplayName("refreshToken - given valid token - returns new tokens")
    void refreshToken_givenValidToken_returnsNewTokens() {
        // Arrange
        User user = createTestUser();
        RefreshTokenRequest request = createRefreshTokenRequest("valid-refresh-token");

        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-refresh-token")).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(any(CustomUserDetails.class))).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(any(CustomUserDetails.class))).thenReturn("new-refresh-token");

        // Act
        LoginResponse response = authService.refreshToken(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("admin");

        verify(jwtTokenProvider).validateToken("valid-refresh-token");
        verify(jwtTokenProvider).getUsernameFromToken("valid-refresh-token");
        verify(userRepository).findByUsername("admin");
    }

    @Test
    @DisplayName("refreshToken - given invalid token - throws UnauthorizedException")
    void refreshToken_givenInvalidToken_throwsUnauthorizedException() {
        // Arrange
        RefreshTokenRequest request = createRefreshTokenRequest("invalid-token");

        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("refreshToken - given deactivated user - throws UnauthorizedException")
    void refreshToken_givenDeactivatedUser_throwsUnauthorizedException() {
        // Arrange
        User inactiveUser = createInactiveUser();
        RefreshTokenRequest request = createRefreshTokenRequest("valid-refresh-token");

        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-refresh-token")).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User account is deactivated");
    }

    // --- Get Current User Tests ---

    @Test
    @DisplayName("getCurrentUser - given existing username - returns user info")
    void getCurrentUser_givenExistingUsername_returnsUserInfo() {
        // Arrange
        User user = createTestUser();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        // Act
        LoginResponse.UserInfo userInfo = authService.getCurrentUser("admin");

        // Assert
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getId()).isEqualTo(1L);
        assertThat(userInfo.getUsername()).isEqualTo("admin");
        assertThat(userInfo.getFullName()).isEqualTo("Admin User");
        assertThat(userInfo.getEmail()).isEqualTo("admin@example.com");
        assertThat(userInfo.getRole()).isEqualTo("ADMIN");
        assertThat(userInfo.getAccessRights()).isEqualTo(255L);

        verify(userRepository).findByUsername("admin");
    }

    @Test
    @DisplayName("getCurrentUser - given nonexistent username - throws ResourceNotFoundException")
    void getCurrentUser_givenNonexistentUsername_throwsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentUser("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }
}
