package com.joven.inventory.context;

import com.joven.inventory.enums.UserRole;
import com.joven.inventory.repository.StoreRepository;
import com.joven.inventory.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StoreContextInterceptor}.
 * Verifies store-access enforcement, super-admin bypass, and context cleanup.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StoreContextInterceptor Tests")
class StoreContextInterceptorTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private StoreContextInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new StoreContextInterceptor(storeRepository);
    }

    @AfterEach
    void tearDown() {
        StoreContext.clear();
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId) {
        CustomUserDetails principal = new CustomUserDetails(
                userId, "user" + userId, "pwd", "User " + userId, UserRole.CASHIER, 0L, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    @DisplayName("preHandle - no header - proceeds without setting store")
    void preHandle_noHeader_proceeds() throws Exception {
        when(request.getHeader("X-Store-Id")).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(StoreContext.getStoreId()).isNull();
    }

    @Test
    @DisplayName("preHandle - super admin (id=1) - bypasses access check")
    void preHandle_superAdmin_bypassesCheck() throws Exception {
        when(request.getHeader("X-Store-Id")).thenReturn("5");
        authenticateAs(1L);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(StoreContext.getStoreId()).isEqualTo(5L);
        // Super admin should NOT trigger the DB access check
        verify(storeRepository, never()).userHasStoreAccess(1L, 5L);
    }

    @Test
    @DisplayName("preHandle - authorized user - sets store context")
    void preHandle_authorizedUser_setsStore() throws Exception {
        when(request.getHeader("X-Store-Id")).thenReturn("3");
        authenticateAs(10L);
        when(storeRepository.userHasStoreAccess(10L, 3L)).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(StoreContext.getStoreId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("preHandle - unauthorized store - returns 403 and blocks")
    void preHandle_unauthorizedStore_forbidden() throws Exception {
        when(request.getHeader("X-Store-Id")).thenReturn("9");
        authenticateAs(10L);
        when(storeRepository.userHasStoreAccess(10L, 9L)).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isFalse();
        assertThat(StoreContext.getStoreId()).isNull();
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN,
                "You do not have access to the requested store");
    }

    @Test
    @DisplayName("preHandle - invalid header - ignores and proceeds")
    void preHandle_invalidHeader_proceeds() throws Exception {
        when(request.getHeader("X-Store-Id")).thenReturn("not-a-number");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(StoreContext.getStoreId()).isNull();
    }

    @Test
    @DisplayName("afterCompletion - clears store context")
    void afterCompletion_clearsContext() {
        StoreContext.setStoreId(7L);

        interceptor.afterCompletion(request, response, new Object(), null);

        assertThat(StoreContext.getStoreId()).isNull();
    }
}
