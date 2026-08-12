package com.joven.inventory.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that populates AuditContext with the current user
 * and IP address from each incoming HTTP request.
 *
 * <p>This interceptor extracts authentication information from the Spring Security
 * context and the client IP address from the servlet request. It sets these values
 * in the thread-local {@link AuditContext} before the request is handled, and clears
 * the context after the request completes to prevent memory leaks.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Component
public class AuditInterceptor implements HandlerInterceptor {

    private static final String ANONYMOUS_USER = "anonymous";

    /**
     * Extracts the authenticated user and IP address, then populates the AuditContext.
     * Called before the request handler method is invoked.
     *
     * @param request  the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler  the chosen handler to execute
     * @return {@code true} to continue processing the request
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String username = resolveUsername();
        String ipAddress = request.getRemoteAddr();
        AuditContext.set(username, ipAddress);
        return true;
    }

    /**
     * Clears the AuditContext after the request has completed to prevent memory leaks.
     * Called after the complete request has finished, regardless of the outcome.
     *
     * @param request  the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler  the handler that was executed
     * @param ex       any exception thrown during handler execution, or null if none
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        AuditContext.clear();
    }

    /**
     * Resolves the username from the Spring Security context.
     * Returns "anonymous" if no authentication is present or the user is not authenticated.
     *
     * @return the authenticated username, or "anonymous" if not authenticated
     */
    private String resolveUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ANONYMOUS_USER;
        }
        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
            return ANONYMOUS_USER;
        }
        return name;
    }
}
