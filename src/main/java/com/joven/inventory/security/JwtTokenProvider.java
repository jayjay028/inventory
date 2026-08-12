package com.joven.inventory.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Component responsible for JWT token generation, validation, and parsing.
 * Uses the jjwt 0.12.x API for all token operations with HMAC-SHA signing.
 *
 * <p>Supports two token types:
 * <ul>
 *     <li>Access token: short-lived, contains user claims (userId, role, accessRights)</li>
 *     <li>Refresh token: longer-lived, contains minimal claims for token renewal</li>
 * </ul>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    /** Base64-encoded secret key for signing tokens */
    @Value("${app.jwt.secret}")
    private String secret;

    /** Access token expiry duration in milliseconds */
    @Value("${app.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    /** Refresh token expiry duration in milliseconds */
    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    /** Issuer claim value for generated tokens */
    @Value("${app.jwt.issuer}")
    private String issuer;

    /** HMAC secret key derived from the configured secret */
    private SecretKey secretKey;

    /**
     * Initializes the HMAC signing key from the configured secret after dependency injection.
     * The secret is used as raw bytes for key generation (must be at least 256 bits).
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT token provider initialized with issuer: {}", issuer);
    }

    /**
     * Generates an access token for the authenticated user. The token contains
     * the username as subject and additional claims: userId, role, and accessRights.
     *
     * @param userDetails the authenticated user's details
     * @return a signed JWT access token string
     */
    public String generateAccessToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiry);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getId())
                .claim("role", userDetails.getRole().name())
                .claim("accessRights", userDetails.getAccessRights())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generates a refresh token for the authenticated user. Contains minimal claims
     * (subject and token type) with a longer expiry for token renewal purposes.
     *
     * @param userDetails the authenticated user's details
     * @return a signed JWT refresh token string
     */
    public String generateRefreshToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiry);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", "refresh")
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validates a JWT token by parsing it with the signing key. Returns false
     * if the token is expired, malformed, or has an invalid signature.
     *
     * @param token the JWT token string to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token expired: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT token is empty or null: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token string
     * @return the username stored in the token's subject claim
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Parses and returns all claims from a JWT token.
     *
     * @param token the JWT token string
     * @return the claims contained in the token
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks whether a JWT token has expired.
     *
     * @param token the JWT token string to check
     * @return true if the token's expiration date is before the current time
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }
}
