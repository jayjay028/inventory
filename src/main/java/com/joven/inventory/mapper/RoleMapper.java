package com.joven.inventory.mapper;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.RoleRequest;
import com.joven.inventory.dto.response.RoleResponse;
import com.joven.inventory.entity.Role;
import org.springframework.data.domain.Page;

/**
 * Utility mapper class for converting between {@link Role} entities
 * and their corresponding DTOs.
 *
 * <p>This class uses static methods and cannot be instantiated.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class RoleMapper {

    /**
     * Private constructor to prevent instantiation.
     */
    private RoleMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a {@link Role} entity to a {@link RoleResponse} DTO.
     *
     * @param role the role entity to convert
     * @return the corresponding role response DTO
     */
    public static RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .accessRights(role.getAccessRights())
                .active(role.getActive())
                .isSystem(role.getIsSystem())
                .build();
    }

    /**
     * Converts a {@link Page} of {@link Role} entities to a {@link PageResponse}
     * of {@link RoleResponse} DTOs.
     *
     * @param page the page of role entities
     * @return the page response containing role response DTOs
     */
    public static PageResponse<RoleResponse> toPageResponse(Page<Role> page) {
        Page<RoleResponse> responsePage = page.map(RoleMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    /**
     * Updates an existing {@link Role} entity with values from a {@link RoleRequest} DTO.
     *
     * @param role    the role entity to update
     * @param request the request DTO containing new values
     */
    public static void updateEntity(Role role, RoleRequest request) {
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setAccessRights(request.getAccessRights());
    }
}
