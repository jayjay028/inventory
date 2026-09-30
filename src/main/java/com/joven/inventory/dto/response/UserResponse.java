package com.joven.inventory.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private Long roleId;
    private String roleName;
    private Long accessRights;
    private Boolean active;
    private LocalDateTime lastLogin;
    /** IDs of stores this user can access. */
    private List<Long> storeIds;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
