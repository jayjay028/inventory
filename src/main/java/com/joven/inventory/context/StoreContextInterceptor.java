package com.joven.inventory.context;

import com.joven.inventory.repository.StoreRepository;
import com.joven.inventory.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that resolves the current store from the {@code X-Store-Id}
 * request header, validates that the authenticated user is authorized to access it,
 * and populates the thread-local {@link StoreContext}.
 *
 * <p>Security: when a store header is present, the interceptor verifies the current
 * user has been granted access to that store (via the user_stores relationship). If
 * the user is not authorized for the requested store, the request is rejected with
 * HTTP 403 Forbidden. This prevents a user from operating a store they were not
 * assigned to, regardless of what the client sends.</p>
 *
 * <p>A missing header does not fail the request, since some endpoints (store listing,
 * authentication, user profile, settings) do not require a store context.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StoreContextInterceptor implements HandlerInterceptor {

    private static final String STORE_ID_HEADER = "X-Store-Id";

    /** User ID of the bootstrap super administrator, who has implicit access to all stores. */
    private static final long SUPER_ADMIN_USER_ID = 1L;

    private final StoreRepository storeRepository;

    /**
     * Reads the {@code X-Store-Id} header, validates the authenticated user's access
     * to the store, and populates the {@link StoreContext} for the current thread.
     * Called before the request handler method is invoked.
     *
     * @param request  the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler  the chosen handler to execute
     * @return {@code true} to continue processing, {@code false} if access is denied
     * @throws Exception if writing the error response fails
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String header = request.getHeader(STORE_ID_HEADER);
        if (header == null || header.isBlank()) {
            return true;
        }

        Long storeId;
        try {
            storeId = Long.parseLong(header.trim());
        } catch (NumberFormatException ex) {
            log.warn("Ignoring invalid {} header value: {}", STORE_ID_HEADER, header);
            return true;
        }

        Long userId = getCurrentUserId();
        if (userId == null) {
            // No authenticated user yet (e.g., public endpoint) — let security handle it.
            return true;
        }

        // Super admin (user ID 1) has implicit access to all stores.
        if (userId == SUPER_ADMIN_USER_ID) {
            StoreContext.setStoreId(storeId);
            return true;
        }

        // Enforce that the user is assigned to the requested store
        if (!storeRepository.userHasStoreAccess(userId, storeId)) {
            log.warn("User id={} attempted to access unauthorized store id={}", userId, storeId);
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "You do not have access to the requested store");
            return false;
        }

        StoreContext.setStoreId(storeId);
        return true;
    }

    /**
     * Clears the {@link StoreContext} after the request has completed to prevent
     * memory leaks. Called after the complete request has finished, regardless of outcome.
     *
     * @param request  the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler  the handler that was executed
     * @param ex       any exception thrown during handler execution, or null if none
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        StoreContext.clear();
    }

    /**
     * Extracts the current authenticated user's ID from the security context.
     *
     * @return the user ID, or null if there is no authenticated CustomUserDetails principal
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }
}
