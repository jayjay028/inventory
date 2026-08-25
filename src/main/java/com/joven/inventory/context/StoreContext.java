package com.joven.inventory.context;

/**
 * Thread-local context that holds the current store ID for the active request.
 * Set by {@code StoreContextInterceptor} at the start of each request from the
 * {@code X-Store-Id} header, and cleared after the request completes.
 *
 * <p>Store-scoped services (stock, sales, shifts, transactions) read the current
 * store from this context to enforce per-store data isolation.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class StoreContext {

    private static final ThreadLocal<Long> CURRENT_STORE = new ThreadLocal<>();

    private StoreContext() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Sets the current store ID for the active thread.
     *
     * @param storeId the store ID resolved from the request
     */
    public static void setStoreId(Long storeId) {
        CURRENT_STORE.set(storeId);
    }

    /**
     * Retrieves the current store ID for the active thread.
     *
     * @return the current store ID, or null if none is set
     */
    public static Long getStoreId() {
        return CURRENT_STORE.get();
    }

    /**
     * Clears the thread-local context to prevent memory leaks.
     * Must be called after request processing completes.
     */
    public static void clear() {
        CURRENT_STORE.remove();
    }
}
