package com.joven.inventory.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory token deny list for invalidating JWT tokens on logout.
 * Tokens are stored until their expiration time, after which they are
 * automatically cleaned up.
 *
 * <p>Note: This implementation is not distributed. In a multi-instance deployment,
 * consider using Redis or a shared store for token invalidation.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Component
public class TokenDenyList {

    private final Map<String, Instant> deniedTokens = new ConcurrentHashMap<>();

    /**
     * Adds a token to the deny list with an expiration time.
     * Also performs cleanup of expired entries.
     *
     * @param token     the JWT token to deny
     * @param expiresAt the time at which the token naturally expires
     */
    public void denyToken(String token, Instant expiresAt) {
        deniedTokens.put(token, expiresAt);
        // Cleanup expired entries
        deniedTokens.entrySet().removeIf(e -> e.getValue().isBefore(Instant.now()));
    }

    /**
     * Checks whether a token has been denied (i.e., invalidated via logout).
     *
     * @param token the JWT token to check
     * @return true if the token is denied and not yet expired, false otherwise
     */
    public boolean isDenied(String token) {
        Instant expiresAt = deniedTokens.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            deniedTokens.remove(token);
            return false;
        }
        return true;
    }
}
