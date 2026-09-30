package com.joven.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating and updating roles.
 * Contains validated fields for role name, description, and bitwise access rights.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    /**
     * The role name. Must not be blank and cannot exceed 50 characters.
     */
    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must not exceed 50 characters")
    private String name;

    /**
     * The role description. Optional, cannot exceed 255 characters.
     */
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    /**
     * The bitwise access rights mask defining the permissions granted by this role.
     */
    @NotNull(message = "Access rights are required")
    private Long accessRights;
}
