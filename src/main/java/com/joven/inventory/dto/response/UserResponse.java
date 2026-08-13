package com.joven.inventory.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO representing a user. Does not include the password field.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private Long accessRights;
    private Boolean active;
    private LocalDateTime lastLogin;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
