package com.joven.inventory.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that enforces bitwise permission checks on methods annotated with
 * {@link RequiresPermission}. Intercepts method invocations before execution and
 * verifies that the authenticated user's access rights satisfy the required permissions.
 *
 * <p>Throws {@link AccessDeniedException} if the user lacks the required permissions,
 * which is handled by Spring Security's exception handling mechanism to return a 403 response.
 *
 * @author Joven Q. Divinagracia Jr.
 * @see RequiresPermission
 * @see Permission
 */
@Aspect
@Component
public class PermissionAspect {

    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    /**
     * Advice that runs before any method annotated with {@link RequiresPermission}.
     * Extracts the current user's access rights from the security context and validates
     * them against the permissions specified in the annotation.
     *
     * @param joinPoint          the join point representing the intercepted method
     * @param requiresPermission the annotation instance containing required permissions
     * @throws AccessDeniedException if the user does not have sufficient permissions
     */
    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new AccessDeniedException("Invalid authentication principal");
        }

        long userRights = userDetails.getAccessRights() != null ? userDetails.getAccessRights() : 0L;
        long[] requiredPermissions = requiresPermission.value();
        boolean requireAll = requiresPermission.requireAll();

        boolean hasAccess;

        if (requireAll) {
            hasAccess = true;
            for (long permission : requiredPermissions) {
                if (!Permission.hasPermission(userRights, permission)) {
                    hasAccess = false;
                    break;
                }
            }
        } else {
            hasAccess = Permission.hasAnyPermission(userRights, requiredPermissions);
        }

        if (!hasAccess) {
            String methodName = joinPoint.getSignature().toShortString();
            log.warn("Access denied for user '{}' on method {}. Required permissions: {}, User rights: {}",
                    userDetails.getUsername(), methodName,
                    formatPermissions(requiredPermissions), userRights);
            throw new AccessDeniedException("Insufficient permissions");
        }
    }

    /**
     * Formats an array of permission values into a readable string for logging.
     *
     * @param permissions the permission values to format
     * @return a formatted string representation of the permissions
     */
    private String formatPermissions(long[] permissions) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < permissions.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(permissions[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
