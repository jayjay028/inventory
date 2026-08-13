package com.joven.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.joven.inventory.dto.request.LoginRequest;
import com.joven.inventory.dto.request.RefreshTokenRequest;
import com.joven.inventory.dto.response.LoginResponse;
import com.joven.inventory.exception.GlobalExceptionHandler;
import com.joven.inventory.exception.UnauthorizedException;
import com.joven.inventory.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc tests for {@link AuthController}.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("POST /api/auth/login - valid credentials returns 200 with tokens")
    void login_givenValidCredentials_returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        LoginResponse response = LoginResponse.builder()
                .accessToken("jwt-access-token")
                .refreshToken("jwt-refresh-token")
                .tokenType("Bearer").expiresIn(1800L)
                .user(LoginResponse.UserInfo.builder()
                        .id(1L).username("admin").fullName("Administrator")
                        .role("ADMIN").accessRights(33554431L).build())
                .build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-access-token"))
                .andExpect(jsonPath("$.data.user.username").value("admin"));
    }

    @Test
    @DisplayName("POST /api/auth/login - invalid credentials returns 401")
    void login_givenInvalidCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        when(authService.login(any())).thenThrow(new UnauthorizedException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - valid token returns 200")
    void refreshToken_givenValidToken_returns200() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        LoginResponse response = LoginResponse.builder()
                .accessToken("new-access-token").refreshToken("new-refresh-token")
                .tokenType("Bearer").expiresIn(1800L).build();

        when(authService.refreshToken(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - invalid token returns 401")
    void refreshToken_givenInvalidToken_returns401() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        when(authService.refreshToken(any())).thenThrow(new UnauthorizedException("Invalid or expired refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
