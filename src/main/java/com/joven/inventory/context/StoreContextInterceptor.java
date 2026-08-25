package com.joven.inventory.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that resolves the current store from the {@code X-Store-Id}
 * request header and populates the thread-local {@link StoreContext}.
 *
 * <p>The store is set before the request handler runs and cleared after the request
 * completes to prevent memory leaks. A missing or unparseable header does not fail
 * the request, since some endpoints (store listing, authentication, settings) do not
 * require a store context.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Component
@Slf4j
public class StoreContextInterceptor implements HandlerInterceptor {

    private static final String STORE_ID_HEADER = "X-Store-Id";

    /**
     * Reads the {@code X-Store-Id} header and, when present and parseable, populates
     * the {@link StoreContext} for the current thread.
     * Called before the request handler method is invoked.
     *
     * @param request  the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler  the chosen handler to execute
     * @return {@code true} to continue processing the request
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader(STORE_ID_HEADER);
        if (header != null && !header.isBlank()) {
            try {
                StoreContext.setStoreId(Long.parseLong(header.trim()));
            } catch (NumberFormatException ex) {
                log.warn("Ignoring invalid {} header value: {}", STORE_ID_HEADER, header);
            }
        }
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
}
