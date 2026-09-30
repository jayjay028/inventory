package com.joven.inventory.service;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.RoleRequest;
import com.joven.inventory.dto.response.RoleResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for role management operations.
 * Provides methods for CRUD operations and status management.
 *
 * <p>Role ID {@link com.joven.inventory.entity.Role#ADMIN_ROLE_ID} is the reserved
 * system ADMIN role and cannot be updated, deactivated, or deleted.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface RoleService {

    /**
     * Retrieves all roles with pagination support.
     *
     * @param pageable the pagination parameters
     * @return a paginated response of role DTOs
     */
    PageResponse<RoleResponse> getAll(Pageable pageable);

    /**
     * Retrieves all active roles.
     *
     * @return a list of active role DTOs
     */
    List<RoleResponse> getAllActive();

    /**
     * Retrieves a single role by its unique identifier.
     *
     * @param id the role ID
     * @return the role response DTO
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the role is not found
     */
    RoleResponse getById(Long id);

    /**
     * Creates a new role.
     *
     * @param request the role request DTO containing creation data
     * @return the created role response DTO
     * @throws com.joven.inventory.exception.DuplicateResourceException if a role with the same name exists
     */
    RoleResponse create(RoleRequest request);

    /**
     * Updates an existing role.
     *
     * @param id      the ID of the role to update
     * @param request the role request DTO containing updated data
     * @return the updated role response DTO
     * @throws com.joven.inventory.exception.ResourceNotFoundException  if the role is not found
     * @throws com.joven.inventory.exception.DuplicateResourceException if another role with the same name exists
     * @throws com.joven.inventory.exception.BadRequestException        if attempting to update the reserved ADMIN role
     */
    RoleResponse update(Long id, RoleRequest request);

    /**
     * Updates the active status of a role.
     *
     * @param id     the ID of the role to update
     * @param active the new active status
     * @return the updated role response DTO
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the role is not found
     * @throws com.joven.inventory.exception.BadRequestException       if attempting to deactivate the reserved ADMIN role
     */
    RoleResponse updateStatus(Long id, boolean active);

    /**
     * Deletes a role.
     *
     * @param id the ID of the role to delete
     * @throws com.joven.inventory.exception.ResourceNotFoundException if the role is not found
     * @throws com.joven.inventory.exception.BadRequestException       if attempting to delete the reserved ADMIN role
     */
    void delete(Long id);
}
