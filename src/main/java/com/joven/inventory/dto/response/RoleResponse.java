package com.joven.inventory.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for role data returned to API consumers.
 * Contains the role identity, description, bitwise access rights, and status flags.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
public class RoleResponse {

    /**
     * The unique identifier of the role.
     */
    private Long id;

    /**
     * The role name.
     */
    private String name;

    /**
     * The role description.
     */
    private String description;

    /**
     * The bitwise access rights mask defining the permissions granted by this role.
     */
    private Long accessRights;

    /**
     * Whether the role is active.
     */
    private Boolean active;

    /**
     * Whether this is a protected system role that cannot be deleted or altered structurally.
     */
    private Boolean isSystem;
}
