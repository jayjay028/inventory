package com.joven.inventory.audit;

/**
 * Thread-local context that holds the current user and IP address for audit trail recording.
 * Set by AuditInterceptor at the start of each request, cleared after request completes.
 *
 * <p>This class uses {@link ThreadLocal} to store request-scoped audit information,
 * ensuring thread safety in a multi-threaded servlet environment.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public class AuditContext {

    private static final ThreadLocal<AuditContext> CONTEXT = new ThreadLocal<>();

    private String currentUser;
    private String ipAddress;

    /**
     * Creates a new AuditContext with the specified user and IP address.
     *
     * @param currentUser the username of the authenticated user
     * @param ipAddress   the IP address of the incoming request
     */
    private AuditContext(String currentUser, String ipAddress) {
        this.currentUser = currentUser;
        this.ipAddress = ipAddress;
    }

    /**
     * Sets the audit context for the current thread.
     *
     * @param user the username of the authenticated user
     * @param ip   the IP address of the incoming request
     */
    public static void set(String user, String ip) {
        CONTEXT.set(new AuditContext(user, ip));
    }

    /**
     * Retrieves the current user from the thread-local context.
     *
     * @return the username of the current user, or "system" if no context is set
     */
    public static String getCurrentUser() {
        AuditContext context = CONTEXT.get();
        return context != null ? context.currentUser : "system";
    }

    /**
     * Retrieves the IP address from the thread-local context.
     *
     * @return the IP address of the current request, or null if no context is set
     */
    public static String getIpAddress() {
        AuditContext context = CONTEXT.get();
        return context != null ? context.ipAddress : null;
    }

    /**
     * Clears the thread-local context to prevent memory leaks.
     * Must be called after request processing completes.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
