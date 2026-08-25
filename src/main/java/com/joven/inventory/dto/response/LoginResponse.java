package com.joven.inventory.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO returned after successful authentication.
 * Contains JWT tokens and basic user information.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;

    /**
     * Nested DTO containing basic user information returned with authentication responses.
     */
    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String role;
        private Long accessRights;
        /** Stores this user is granted access to. */
        private List<StoreInfo> stores;
    }

    /**
     * Nested DTO containing minimal store information for the store selector.
     */
    @Data
    @Builder
    public static class StoreInfo {
        private Long id;
        private String code;
        private String name;
    }
}
